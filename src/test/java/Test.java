import java.lang.Character.UnicodeScript;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.zhangzhen.redis.locks.DistributedLockForRedis_V1;

import redis.clients.jedis.JedisPool;

public class Test {

	public static void main(String[] args) {
        Service2 service = new Service2();
        for (int i = 0; i < 1000; i++) {
            ThreadA2 threadA = new ThreadA2(service);
            threadA.start();
        }

				
	}
}
