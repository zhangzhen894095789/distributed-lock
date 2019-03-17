

/**
 * Created by zz on 2017/4/20.
 */
public class ThreadA2 extends Thread {
    private Service2 service;

    public ThreadA2(Service2 service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.seckill();
    }
}
