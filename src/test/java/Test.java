import java.lang.Character.UnicodeScript;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.zhangzhen.redis.locks.DistributedLockForRedis_V1;

import redis.clients.jedis.JedisPool;

public class Test {
	final static JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);
	final static DistributedLockForRedis_V1 distributedLock = new DistributedLockForRedis_V1(new JedisPool("127.0.0.1", 6379), "123");
//	final static ThreadPoolExecutor newFixedThreadPool = new ThreadPoolExecutor(100, 100, 6000000L,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>()); 
	final static ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(100) ;
	public static void main(String[] args) {
		try {
		for (int i = 0; i < 6 ; i++) {
	
			newFixedThreadPool.execute(new Runnable() {
				
				public void run() {
					boolean tryAcquire = distributedLock.tryAcquire();
					if(tryAcquire) {
						System.out.println("线程："+Thread.currentThread().getName()+" 获取锁");
						try {
							Thread.sleep(5000);
							distributedLock.release();
							System.out.println("线程："+Thread.currentThread().getName()+" 释放锁成功");
						} catch ( Exception e) {
							e.printStackTrace();
						}
					}else {
						int m = 0;
						while(true) {
							try {
								//if(m == 10 ) {System.out.println("线程："+Thread.currentThread().getName()+"获取锁失败");break;}
								System.out.println("线程："+Thread.currentThread().getName()+"第"+(m++)+"次尝试获取锁");
								tryAcquire = distributedLock.tryAcquire();
								if(tryAcquire) {
									System.out.println("线程："+Thread.currentThread().getName()+" 获取锁");
									try {
										Thread.sleep(35000);
										distributedLock.release();
										System.out.println("线程："+Thread.currentThread().getName()+" 释放锁成功");
									} catch ( Exception e) {
										e.printStackTrace();
									}
									break;
								}else {
									Thread.sleep(1000L);
								}

							} catch ( Exception e) {
								e.printStackTrace();
							}
						
						}
					}
					
				}
			});
			System.out.println("添加"+i+"个成功");
			
		}
		
		System.out.println("一结束");
		} catch ( Exception e) {
			e.printStackTrace();
		}
				
	}
}
