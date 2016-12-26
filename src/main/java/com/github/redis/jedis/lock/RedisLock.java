package com.github.redis.jedis.lock;

import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁
 *
 * @author liuchunlong(631521383@qq.com)
 */
public class RedisLock {

    private static final Lock NO_LOCK = new Lock(new UUID(0l,0l), 0l);//超时锁     uuid:00000000-0000-0000-0000-000000000000     expiryTime:0

    private static final int ONE_SECOND = 1000;//1秒

    public static final int DEFAULT_EXPIRY_MILLIS = Integer.getInteger("com.github.redis.jedis.lock.expiry.millis", 60 * ONE_SECOND);//默认锁的过期时长
    public static final int DEFAULT_ACQUIRE_TIMEOUT_MILLIS = Integer.getInteger("com.github.redis.jedis.lock.acquire.timeout.millis", 10 * ONE_SECOND);//默认锁的请求超时时长
    public static final int DEFAULT_ACQUIRE_RESOLUTION_MILLIS = Integer.getInteger("com.github.jedis.lock.acquire.resolution.millis", 100);//循环请求分布式锁线程休眠时长

    private final Jedis jedis;

    private final String lockKey;//锁在Redis中的Key标记 (ex. account:1, ...)
    private final int lockExpiryInMillis;//锁的过期时长
    private final int acquireTimeoutInMillis;//锁的请求超时时长
    private final UUID lockUUID;//锁的唯一标识

    private Lock lock = null;

    /**
     * 详细构造方法:<br>
     *      - 使用默认请求超时时长 10秒;<br>
     *      - 使用默认锁的过期时长 60秒;<br>
     *      - 锁的唯一标识UUID采用随机生成策略;<br>
     * @param jedis                     Jedis对象
     * @param lockKey                   锁在Redis中的Key标记 (ex. account:1, ...)
     */
    public RedisLock(Jedis jedis, String lockKey) {
        this(jedis, lockKey, DEFAULT_ACQUIRE_TIMEOUT_MILLIS, DEFAULT_EXPIRY_MILLIS);
    }

    /**
     * 详细构造方法:<br>
     *      - 使用默认锁的过期时长 60秒;<br>
     *      - 锁的唯一标识UUID采用随机生成策略;<br>
     * @param jedis                     Jedis对象
     * @param lockKey                   锁在Redis中的Key标记 (ex. account:1, ...)
     * @param acquireTimeoutInMillis    请求超时时长(单位:毫秒)
     */
    public RedisLock(Jedis jedis, String lockKey, int acquireTimeoutInMillis) {
        this(jedis, lockKey, acquireTimeoutInMillis, DEFAULT_EXPIRY_MILLIS);
    }

    /**
     * 详细构造方法:<br>
     *      - 锁的唯一标识UUID采用随机生成策略
     * @param jedis                     Jedis对象
     * @param lockKey                   锁在Redis中的Key标记 (ex. account:1, ...)
     * @param acquireTimeoutInMillis    请求超时时长(单位:毫秒)
     * @param lockExpiryInMillis        锁的过期时长(单位:毫秒)
     */
    public RedisLock(Jedis jedis, String lockKey, int acquireTimeoutInMillis, int lockExpiryInMillis) {
        this(jedis, lockKey, acquireTimeoutInMillis, lockExpiryInMillis, UUID.randomUUID());
    }

    /**
     * 详细构造方法
     * @param jedis                     Jedis对象
     * @param lockKey                   锁在Redis中的Key标记 (ex. account:1, ...)
     * @param acquireTimeoutInMillis    请求超时时长(单位:毫秒)
     * @param lockExpiryInMillis        锁的过期时长(单位:毫秒)
     * @param uuid                      锁的唯一标识
     */
    public RedisLock(Jedis jedis, String lockKey, int acquireTimeoutInMillis, int lockExpiryInMillis, UUID uuid) {

        this.jedis = jedis;
        this.lockKey = lockKey;
        this.acquireTimeoutInMillis = acquireTimeoutInMillis;
        this.lockExpiryInMillis = lockExpiryInMillis;
        this.lockUUID = uuid;
    }


    /**
     * 获取锁的唯一标识UUID
     * @return      lockUUID
     */
    public UUID getLockUUID() {
        return this.lockUUID;
    }

    /**
     * 锁在Redis中的Key标记
     * @return      lockKey
     */
    public String getLockKey() {
        return this.lockKey;
    }





    /**
     * 请求分布式锁
     * @return  请求到锁返回true,超时返回false
     * @throws InterruptedException
     *          线程中断异常
     */
    public synchronized boolean acquire() throws InterruptedException {
        return acquire(jedis);
    }

    /**
     * 请求分布式锁
     * @param jedis     Jedis对象
     * @return          请求到锁返回true,超时返回false
     * @throws InterruptedException     线程中断异常
     *          线程中断异常
     */
    protected synchronized boolean acquire(Jedis jedis) throws InterruptedException {

        //采用`循环请求锁`的方式,每次循环线程休眠100毫秒,直至请求锁超时
        int timeout = acquireTimeoutInMillis;
        while (timeout >= 0) {

            //创建一个新锁
            final Lock newLock = new Lock(lockUUID, System.currentTimeMillis() + lockExpiryInMillis);

            //将当前锁存入Redis中
            //1.如果成功存入,Redis中不存在锁,获取锁成功;
            //2.否则,Redis中已存在锁,获取锁失败;
            if (jedis.setnx(lockKey, newLock.toString()) == 1) {
                this.lock = newLock;
                return true;
            }

            //Redis中已存在锁,获取锁失败,则需要进行如下操作:
            //判断Redis中已存在的锁是否过期,如果过期则设置新锁,即获取锁成功
            final String currentValueStr = jedis.get(lockKey);//获取Redis中已存在锁的值
            final Lock currentLock = Lock.fromString(currentValueStr);

            //如果锁已超时或者锁是当前线程的,则重新获取锁.
            if (currentLock.isExpiredOrMine(lockUUID)) {
                String oldValueStr = jedis.getSet(lockKey, newLock.toString());
                //这里还有个后置条件:会对锁进行校验,jedis.get()和jedis.getSet()获取的锁必须是同一锁,重新获取锁才成功.
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    this.lock = newLock;
                    return true;
                }
            }

            timeout -= DEFAULT_ACQUIRE_RESOLUTION_MILLIS;
            TimeUnit.MILLISECONDS.sleep(DEFAULT_ACQUIRE_RESOLUTION_MILLIS);

        }
        return false;
    }

    /**
     * 重新获取锁
     * @return          如果取得锁返回true,否则返回false
     * @throws InterruptedException     线程中断异常
     */
    public boolean renew() throws InterruptedException {

        final Lock lock = Lock.fromString(jedis.get(lockKey));
        if (!lock.isExpiredOrMine(lockUUID)) {
            return false;
        }
        return acquire(jedis);
    }

    /**
     * 释放锁
     */
    public synchronized void release() {
        release(jedis);
    }

    protected synchronized void release(Jedis jedis) {
        if (isLocked()) {
            jedis.del(lockKey);
            this.lock = null;
        }
    }

    /**
     * 判断当前是否获取锁
     * @return  返回布尔类型的值,是否已获得锁
     */
    public synchronized boolean isLocked() {
        return this.lock != null;
    }

    public synchronized long getLockExpiryTimeInMillis() {
        return this.lock.getExpiryTime();
    }

    /**
     * 锁
     */
    protected static class Lock {
        private UUID uuid;//锁的唯一标识
        private long expiryTime;//锁的过期时间

        protected Lock(UUID uuid, long expiryTimeInMillis) {
            this.uuid = uuid;
            this.expiryTime = expiryTimeInMillis;
        }

        /**
         * 解析字符串,根据指定的UUID值和过期时间构造Lock
         * @param text  字符串参数,参数格式:"*:*"
         * @return      Lock    字符串转化的锁对象
         */
        protected static Lock fromString(String text) {

            try {
                String[] parts = text.split(":");
                UUID theUUID = UUID.fromString(parts[0]);
                long theTime = Long.parseLong(parts[1]);
                return new Lock(theUUID, theTime);
            } catch (Exception e) {
                return NO_LOCK;
            }
        }

        public UUID getUUID() {
            return uuid;
        }

        public long getExpiryTime() {
            return expiryTime;
        }

        @Override
        public String toString() {
            return uuid.toString()+":"+expiryTime;
        }

        /**
         * 判断锁是否超时,如果锁的过期时间小于当前系统时间,则判定锁超时
         * @return      返回布尔类型的值
         */
        boolean isExpired() {
            return getExpiryTime() < System.currentTimeMillis();
        }

        /**
         * 判断锁是否超时或者锁是当前线程拥有的锁
         * @param otherUUID     锁的唯一标识
         * @return      返回布尔类型的值
         */
        boolean isExpiredOrMine(UUID otherUUID) {
            return this.isExpired() || this.getUUID().equals(otherUUID);
        }



    }
}
