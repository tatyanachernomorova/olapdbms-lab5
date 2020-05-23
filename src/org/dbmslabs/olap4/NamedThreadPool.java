package org.dbmslabs.olap4;

import java.util.ArrayList;
import java.util.concurrent.*;

public class NamedThreadPool {
    private final EsThreadFactory threadFactory;
    private ArrayList<Thread> threads;
    private final int threadCount;
    private final ThreadPoolExecutor threadPoolExecutor;

    NamedThreadPool(String poolName, int threadCount) {
        this.threadFactory = new EsThreadFactory(poolName);
        this.threadCount = threadCount;
        this.threadPoolExecutor = new ThreadPoolExecutor(threadCount, threadCount, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20), threadFactory);
    }

    public Future<?> execute(Callable<?> c ) {
        return threadPoolExecutor.submit(c);
    }
}
