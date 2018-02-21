package com.hutoma.api.thread;

import com.google.inject.Singleton;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
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
    public ThreadPool(final IThreadConfig config, final ILogger logger) {
        // create a thread pool with a hard maximum
        // once (maxPoolSize) threads are already running no new threads will be started
        // and exceptions will be thrown
        this.executorService = new ThreadPoolExecutorLogged(
                logger,
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

    public static class ThreadPoolExecutorLogged extends ThreadPoolExecutor {

        public ThreadPoolExecutorLogged(final ILogger logger, final int corePoolSize, final int maximumPoolSize,
                                        final long keepAliveTime, final TimeUnit unit,
                                        final BlockingQueue<Runnable> workQueue,
                                        final RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
            this.logger = logger;
        }

        private final ILogger logger;
        private static final String logFrom = "ThreadPoolExec";

        private enum Action {
            Start,
            End
        }

        private void logAction(Runnable r, Action action) {

            // total size of the pool
            int poolSize = this.getPoolSize();

            // how many threads are currently active (one less if this is an end action)
            int activeCount = this.getActiveCount() - ((action == Action.End) ? 1 : 0);

            // create logmap, add the Runnable hashcode to match starts to ends
            LogMap logMap = LogMap.map("Op", action.name())
                    .put("PoolSize", poolSize)
                    .put("ActiveCount", activeCount)
                    .put("TaskHash", (r == null) ? 0 : r.hashCode());

            // log
            logger.logDebug(logFrom, String.format("Thread %s", action.name()),
                    logMap);
        }

        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            logAction(r, Action.Start);
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);
            logAction(r, Action.End);
        }

    }

}


