

import com.github.zhangzhen.redis.locks.DistributedLockForRedis_V1;
import com.github.zhangzhen.redis.locks.DistributedLockForRedis_V2;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by zz on 2017/4/20.
 */
public class Service2 {
    private static JedisPool pool = null;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(1000);
        // 设置最大空闲数
        config.setMaxIdle(100);
        // 设置最大等待时间
        config.setMaxWaitMillis(1000 * 100);
        // 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, "127.0.0.1", 6379, 3000);
    }

    DistributedLockForRedis_V1 lock = new DistributedLockForRedis_V1(pool, "resource");

    int n = 500;

    public void seckill() {
        // 返回锁的value值，供释放锁时候进行判断
    	try {
        boolean indentifier = lock.tryAcquire();
        if (indentifier) {
        	
            try {
//            	Thread.sleep(5000);
            	lock.release();
            	
            } catch (InterruptedException e) {
            	e.printStackTrace();
            }
//        	System.out.println(--n);
		}else {
			
			while(true) {
//				System.out.println(Thread.currentThread().getName() + "尝试获取锁"+System.currentTimeMillis());
				indentifier = lock.tryAcquire();
				if(indentifier) {
					  try {
//						  Thread.sleep(5000);
			            	lock.release();
			            } catch (InterruptedException e) {
			            	e.printStackTrace();
			            }
			        	System.out.println(--n);
					break;
				} 
			}
			
		}
   
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
