package com.github.redis.jedis.lock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * 单元测试
 *
 * @author liuchunlong <631521383@qq.com></>
 */
public class RedisLockTest {

    private static final String LOCK_KEY = "foo";
    private static final int ACQUIRE_TIMEOUT = RedisLock.DEFAULT_ACQUIRE_RESOLUTION_MILLIS * 3;//请求超时时长(单位:毫秒)
    private static final int LEASE_TIME = 5000;//锁的过期时长(单位:毫秒)

    private Jedis jedis;
    private RedisLock lock;

    @Before
    public void setup() {
        jedis = mock(Jedis.class);
        lock = new RedisLock(jedis, LOCK_KEY, ACQUIRE_TIMEOUT, LEASE_TIME);
    }

    /**
     * <tr/>acquire()方法获取锁;
     * <tr/>setnx()方法至少调用一次;
     * <tr/>获取锁的时间在55毫秒内;
     *
     * @throws Exception
     */
    @Test
    public void shouldInvokeSetnxOnAcquire() throws Exception {
        TimeMatcher expectedTime = new TimeMatcher(System.currentTimeMillis() + LEASE_TIME);

        //获取锁
        lock.acquire();

        //jedis调用setnx()方法至少一次,并且参数校验:获取锁的时间在55毫秒内
        verify(jedis, atLeastOnce()).setnx(eq(LOCK_KEY), argThat(expectedTime));
    }

    /**
     * </tr>设置通过setnx()方法返回值为1,即成功获取锁;
     * </tr>判断是否获取锁;
     * @throws Exception
     */
    @Test
    public void shouldLockOnSuccessfulAcquire() throws Exception {
        //设置jedis通过方法setnx()加锁时,方法返回1,即加锁成功.
        whenSettingTokenOnRedisBeSuccessful();
        //则result一定返回true
        boolean result = lock.acquire();
        assertTrue(result);
        //判断是否加锁
        assertLockedDirectly();
    }


    private void assertLockedDirectly() {
        //判断是否加锁
        assertLocked();
        //jedis调用setnx()方法至少一次
        verify(jedis, atLeastOnce()).setnx(eq(LOCK_KEY), anyString());
    }

    /**
     * 判断是否加锁,并且判断`当前时间+锁的过期时长`与实际加的锁的过期时间比较,两者的时间差是否在55毫秒以内
     */
    private void assertLocked() {
        //判断是否已加锁
        assertTrue(lock.isLocked());
        //判断当前请求加锁与实际加锁,两者的锁过期时间是否在55毫秒以内
        assertEquals((double) lock.getLockExpiryTimeInMillis(), (double) System.currentTimeMillis() + LEASE_TIME, 55);
    }

    /**
     * 设置jedis通过方法setnx()加锁时,方法返回1,即加锁成功.
     */
    private void whenSettingTokenOnRedisBeSuccessful() {
        when(jedis.setnx(eq(LOCK_KEY), anyString())).thenReturn(1l);
    }


    /**
     * 自定义参数匹配器:
     *      用于校验`当前时间+锁的过期时长`与实际加的锁的过期时间比较,两者的时间差是否在55毫秒以内,
     *      也即判断加锁所需要的实际时间
     */
    protected static class TimeMatcher extends ArgumentMatcher<String> {

        private long expectedTimeInMillis;

        public TimeMatcher(long expectedTimeInMillis) {
            this.expectedTimeInMillis = expectedTimeInMillis;
        }

        public boolean matches(Object target) {

            //根据参数target构造锁,并获取锁的过期时间
            long expiryTimeInMillis = RedisLock.Lock.fromString((String) target).getExpiryTime();
            return Math.abs(expectedTimeInMillis - expiryTimeInMillis) < 55;
        }
    }


}
