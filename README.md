# redis-jedis-lock
redis-jedis-lock使用起来非常简便,它是使用Redis数据库和Jedis驱动的分布式锁的简单实现


## 引入
通过Maven依赖:
```
<dependency>
    <groupId>com.github.itTalks</groupId>
    <artifactId>redis-jedis-lock</artifactId>
    <version>1.0.0</version>
    <type>jar</type>
    <scope>compile</scope>
</dependency>
```

## 使用
```
GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
final JedisPool pool = new JedisPool(poolConfig, host, port, timeout, password);
Jedis jedis = pool.getResource();

RedisLock redisLock = new RedisLock(jedis, lockKey, timeoutMsecs, expireMsecs);

try {
    if (redisLock.acquire()) { // 启用锁
        //执行业务逻辑
    } else {
        logger.info("The time wait for lock more than [{}] ms ", timeoutMsecs);
    }
} catch (Throwable t) {
    // 分布式锁异常
    logger.warn(t.getMessage(), t);
} finally {
    if (redisLock != null) {
        try {
            redisLock.release();// 则解锁
        } catch (Exception e) {
        }
    }
    if (jedis != null) {
        try {
            pool.returnResource(jedis);// 还到连接池里
        } catch (Exception e) {
        }
    }
}


```

## 结束语
如果您有更好的优化,欢迎联系我<631521383@qq.com>. Thx.

## License

The Apache Software License, Version 2.0