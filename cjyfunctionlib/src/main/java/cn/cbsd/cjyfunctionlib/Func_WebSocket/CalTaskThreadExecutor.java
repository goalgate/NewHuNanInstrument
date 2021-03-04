package cn.cbsd.cjyfunctionlib.Func_WebSocket;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CalTaskThreadExecutor {
    private static final ExecutorService instance = new ThreadPoolExecutor(1, 3,
            0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "SingleTaskPoolThread #" + mCount.getAndIncrement());
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    Log.e("TAG", "超了");
                    executor.remove(r);
                }
            });

    public static ExecutorService getInstance() {
        return instance;
    }
}
