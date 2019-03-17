package com.github.zhangzhen.redis.locks;

/**
 * Created by zz on 2017/4/20.
 */
public class ThreadA extends Thread {
    private Service service;

    public ThreadA(Service service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.seckill();
    }
}
