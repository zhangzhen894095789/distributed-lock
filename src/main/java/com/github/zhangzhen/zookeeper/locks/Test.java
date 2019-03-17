package com.github.zhangzhen.zookeeper.locks;

/**
 * Created by zz on 2017/4/20.
 */
public class Test {
    static int n = 1000;

    public static void secskill() {
        System.out.println(--n);
    }

    public static void main(String[] args) {
    	long start = System.currentTimeMillis();
        Runnable runnable = new Runnable() {
            public void run() {
                DistributedLockForZookeeper_V1 lock = null;
                try {
                    lock = new DistributedLockForZookeeper_V1("127.0.0.1:2181", "test1");
                    lock.lock();
                    // 拿到了锁那么这里就去执行相关业务代码
                    secskill();
                    System.out.println(Thread.currentThread().getName() + "正在运行");
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };

        for (int i = 0; i < 1000; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
        
        
    }
    
//    @org.junit.Test
//    public void test1() {
//    	String CURRENT_LOCK = "/locks/test1_lock_0000002755";
//    	String prevNode = CURRENT_LOCK.substring(CURRENT_LOCK.lastIndexOf("/") + 1);
//    	System.out.println(prevNode);
//    }
}
