package com.github.zhangzhen.redis.locks;

/**
 * Created by zz on 2017/4/20.
 */
public class Test {
    public static void main(String[] args) {
        Service service = new Service();
        for (int i = 0; i < 500; i++) {
            ThreadA threadA = new ThreadA(service);
            threadA.start();
        }
    }
}
