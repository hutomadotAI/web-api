package com.hutoma.api.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.inject.Inject;

/***
 * A proxy for the singleton main threadpool that does
 * not allow the caller to shutdown the pool or anything like that
 */
public class ThreadSubPool {

    // copy of executor service from the big pool
    private final ExecutorService executorService;

    @Inject
    public ThreadSubPool(ThreadPool threadPool) {
        this.executorService = threadPool.getExecutorService();
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param runnable runnable task
     * @return a future
     */
    public Future submit(Runnable runnable) {
        return this.executorService.submit(runnable);
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param callable callable task
     * @return a future
     */
    public Future submit(Callable callable) {
        return this.executorService.submit(callable);
    }

}
