package com.hutoma.api.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.inject.Inject;

/***
 * Create a dynamic sub-pool of threads out of the big queue
 */
public class ThreadSubPool implements AutoCloseable {

    // copy of executor service from the big pool
    private ExecutorService executorService;

    // a list of threads that we have opened
    private ConcurrentLinkedQueue<Future> threads;

    @Inject
    public ThreadSubPool(ThreadPool threadPool) {
        this.executorService = threadPool.getExecutorService();
        this.threads = new ConcurrentLinkedQueue<>();
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param runnable runnable task
     * @return a future
     */
    public Future submit(Runnable runnable) {
        Future future = this.executorService.submit(runnable);
        this.threads.add(future);
        return future;
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param callable callable task
     * @return a future
     */
    public Future submit(Callable callable) {
        Future future = this.executorService.submit(callable);
        this.threads.add(future);
        return future;
    }

    /***
     * Cancels all tasks immediately
     * If they were running then they get interrupted.
     */
    public void cancelAll() {
        Future future = this.threads.poll();
        while (future != null) {
            future.cancel(true);
            future = this.threads.poll();
        }
    }

    @Override
    public void close() {
        cancelAll();
    }
}