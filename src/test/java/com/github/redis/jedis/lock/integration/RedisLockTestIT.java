package com.github.redis.jedis.lock.integration;

import com.github.redis.jedis.lock.RedisLock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 *  集成测试
 *
 *  通过传入必要的虚拟机参数,加载:mvn failsafe:integration-test,例如:
 *  mvn -Dredis.host=localhost -Dredis.auth=foobared failsafe:integration-test
 */
public class RedisLockTestIT {

    private static final Logger logger = LoggerFactory.getLogger("RedisLockTestIT.class");

    private static final String REDIS_HOST = System.getProperty("redis.host", "127.0.0.1");
    private static final Integer REDIS_PORT = Integer.getInteger("redis.port", Protocol.DEFAULT_PORT);
    private static final String REDIS_AUTH = System.getProperty("redis.auth");

    @BeforeClass
    public static void setup() {

        String hostinfo  = String.format("endpoint=%s:%d, auth=%s", REDIS_HOST, REDIS_PORT, REDIS_AUTH);

        logger.info("Using redis at " + hostinfo);

        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);

        try {
            connect(jedis);
        } catch (Exception e) {
            logger.error("Unable to connect to Jedis - " + hostinfo);
            fail("Unable to connect to Jedis - " + hostinfo);
        } finally {
            disconnect(jedis);
        }

    }

    /**
     * 测试lock,lock2竞争加锁,以及锁的释放
     * @throws InterruptedException
     */
    @Test
    public void testAcquire() throws InterruptedException {

        Jedis jedis = connect();

        //lock加锁成功
        RedisLock lock = new RedisLock(jedis, "testlock");
        assertTrue(lock.acquire());

        //lock2加锁失败
        RedisLock lock2 = new RedisLock(jedis, "testlock");
        assertFalse(lock2.acquire());

        //lock锁释放
        lock.release();

        //lock2加锁成功
        lock2 = new RedisLock(jedis, "testlock", 1000);
        assertTrue(lock2.acquire());

        //lock2释放锁
        lock2.release();
    }

    /**
     * 基于该锁是自己的锁,重新加锁成功
     * @throws InterruptedException
     */
    @Test
    public void testRenew() throws InterruptedException {

        Jedis jedis = connect();

        RedisLock lock = new RedisLock(jedis, "testlock");//锁的过期时长默认60s
        assertTrue(lock.acquire());

        TimeUnit.MILLISECONDS.sleep(2000l);

        //基于该锁是自己的锁,重新加锁成功
        assertTrue(lock.renew());

        lock.release();

        //lock2加锁成功
        RedisLock lock2 = new RedisLock(jedis, "testlock", 1000);
        assertTrue(lock2.acquire());
        lock2.release();

    }

    @Test
    public void testConcurrency() throws InterruptedException {

        final int count = 10;

        ConcurrentLocker[] lockers = new ConcurrentLocker[] {
                new ConcurrentLocker(count, "thread1"),
                new ConcurrentLocker(count, "thread2"),
                new ConcurrentLocker(count, "thread3")
        };

        for (ConcurrentLocker locker : lockers) {
            locker.start();
        }

        //三个线程并行执行
        for (ConcurrentLocker locker : lockers) {
            locker.join();
        }

        for (ConcurrentLocker locker : lockers) {
            assertEquals(count, locker.count());
        }

    }


    private class ConcurrentLocker extends Thread {

        private final int times;
        private int counter;

        public ConcurrentLocker(int times, String threadName) {
            super(threadName);
            this.times = times;
            this.counter = 0;
        }

        public void run() {
            Jedis jedis = connect();
            try {
                for (int i = 0; i < times; i++) {
                    RedisLock lock = new RedisLock(jedis, "testlock", 15000, 200);

                    try {
                        if (lock.acquire()) {
                            counter++;
                            TimeUnit.MILLISECONDS.sleep(250l);
                            lock.release();
                            logger.info("线程[ " + Thread.currentThread().getName() + " ] 第 " + i +" 次获取锁成功");
                        } else {
                            logger.info("线程[ " + Thread.currentThread().getName() + " ] 第 " + i +" 次获取锁失败");
                        }
                        TimeUnit.MILLISECONDS.sleep(100l);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                }
            } finally {
                disconnect(jedis);
            }
        }

        public int count() {
            return counter;
        }

    }



    private Jedis connect() {
        Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        connect(jedis);
        return jedis;
    }

    private static void connect(Jedis jedis) {
        jedis.connect();
        if (REDIS_AUTH != null) {
            jedis.auth(REDIS_AUTH);
        }
    }

    private static void disconnect(Jedis jedis) {
        try {
            jedis.disconnect();
        } catch (Throwable ignore) {
        }
    }
}
