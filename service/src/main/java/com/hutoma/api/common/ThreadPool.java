package com.hutoma.api.common;

import com.google.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 * Creates a single pool of threads with an upper limit
 */
@Singleton
public class ThreadPool {

    // thread provider
    private ExecutorService executorService;

    @Inject
    public ThreadPool(Config config) {
        // create a thread pool with a hard maximum
        // once (maxPoolSize) threads are already running no new threads will be started
        // and exceptions will be thrown
        this.executorService = new ThreadPoolExecutor(
                16,
                config.getThreadPoolMaxThreads(),
                config.getThreadPoolIdleTimeMs(), TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /***
     * Return the executor service
     * This should only ever be called by ThreadSubPool
     * @return
     */
    ExecutorService getExecutorService() {
        return this.executorService;
    }
}


