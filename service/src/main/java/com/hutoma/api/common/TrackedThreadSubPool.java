package com.hutoma.api.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import javax.inject.Inject;

/***
 * Create a dynamic sub-pool of threads out of the big queue
 */
public class TrackedThreadSubPool extends ThreadSubPool implements AutoCloseable {

    // a list of threads that we have opened
    private final ConcurrentLinkedQueue<Future> threads;

    @Inject
    public TrackedThreadSubPool(ThreadPool threadPool) {
        super(threadPool);
        this.threads = new ConcurrentLinkedQueue<>();
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param runnable runnable task
     * @return a future
     */
    @Override
    public final Future submit(final Runnable runnable) {
        Future future = super.submit(runnable);
        this.threads.add(future);
        return future;
    }

    /***
     * Submit a task. A thread will be pulled out of the big pool.
     * @param callable callable task
     * @return a future
     */
    @Override
    public final Future submit(Callable callable) {
        Future future = super.submit(callable);
        this.threads.add(future);
        return future;
    }

    /***
     * Cancels all tasks immediately
     * If they were running then they get interrupted.
     */
    public final void cancelAll() {
        Future future = this.threads.poll();
        while (future != null) {
            future.cancel(true);
            future = this.threads.poll();
        }
    }

    @Override
    public final void close() {
        cancelAll();
    }
}