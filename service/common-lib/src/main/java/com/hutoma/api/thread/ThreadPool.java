package com.hutoma.api.thread;

import com.google.inject.Singleton;

import java.lang.reflect.Field;
import java.util.concurrent.*;
import javax.inject.Inject;

/**
 * Creates a single pool of threads with an upper limit
 */
@Singleton
public class ThreadPool {

    // thread provider
    private ExecutorService executorService;

    @Inject
    public ThreadPool(final IThreadConfig config) {
        // create a thread pool with a hard maximum
        // once (maxPoolSize) threads are already running no new threads will be started
        // and exceptions will be thrown
        this.executorService = new ThreadPoolExecutorLogged(
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

        public ThreadPoolExecutorLogged(final int corePoolSize, final int maximumPoolSize,
                                        final long keepAliveTime, final TimeUnit unit,
                                        final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        ConcurrentHashMap<Object, String> names = new ConcurrentHashMap<>();
        ConcurrentHashMap<Runnable, Object> tasks = new ConcurrentHashMap<>();

        private String getTag() {
            String where = "";
            for(StackTraceElement element: Thread.currentThread().getStackTrace()) {
                String s = element.toString();
                if (!s.isEmpty() && !s.contains("Thread")) {
                    where = s;
                    break;
                }
            }
            return where;
        }

        @Override
        public Future<?> submit(final Runnable task) {
            names.put(task, getTag());
            return super.submit(task);
        }

        @Override
        public <T> Future<T> submit(final Runnable task, final T result) {
            names.put(task, getTag());
            return super.submit(task, result);
        }

        @Override
        public <T> Future<T> submit(final Callable<T> task) {
            names.put(task, getTag());
            return super.submit(task);
        }

        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            Object realTask = JobDiscoverer.findRealTask(r);
            tasks.put(r, realTask);
            System.out.println(String.format("THREAD starting %s %s %d",
                    Integer.toHexString(r.hashCode()),
                    names.getOrDefault(realTask, "none"),
                    tasks.size() - 1));
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            super.afterExecute(r, t);
            Object realTask = tasks.getOrDefault(r, "notask");
            System.out.println(String.format("THREAD ending %s %s %d",
                    Integer.toHexString(r.hashCode()),
                    names.getOrDefault(realTask, "none"),
                    tasks.size() - 1));
            names.remove(realTask);
            tasks.remove(r);
        }
    }

    public static class JobDiscoverer {

        private final static Field callableInFutureTask;
        private static final Class<? extends Callable> adapterClass;
        private static final Field runnableInAdapter;

        static {
            try {
                callableInFutureTask =
                        FutureTask.class.getDeclaredField("callable");
                callableInFutureTask.setAccessible(true);
                adapterClass = Executors.callable(new Runnable() {
                    public void run() { }
                }).getClass();
                runnableInAdapter =
                        adapterClass.getDeclaredField("task");
                runnableInAdapter.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public static Object findRealTask(Runnable task) {
            if (task instanceof FutureTask) {
                try {
                    Object callable = callableInFutureTask.get(task);
                    if (adapterClass.isInstance(callable)) {
                        return runnableInAdapter.get(callable);
                    } else {
                        return callable;
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            throw new ClassCastException("Not a FutureTask");
        }
    }

}


