package com.hutoma.api.thread;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by David MG on 31/01/2017.
 */
public class TestThreadPool {

    TestableThreadPool pool;
    ArrayList<TestThread> tasks;

    @Before
    public void setup() {
        this.tasks = new ArrayList<>();
    }

    @After
    public void cleanup() {
        if (this.pool != null) {
            this.pool.shutdownNow();
        }
    }

    @Test
    public void startEmpty() {
        this.pool = getPool(1024, 100);
        Assert.assertEquals(0, this.pool.getPoolSize());
    }

    @Test
    public void runOne() throws ExecutionException, InterruptedException {
        this.pool = getPool(1024, 100);
        TrackedThreadSubPool subPool = new TrackedThreadSubPool(this.pool);
        makeTasksAndAwait(subPool, 50, 1);
        Assert.assertEquals(1, this.pool.getPoolSize());
    }

    @Test
    public void runMany() throws ExecutionException, InterruptedException {
        this.pool = getPool(1024, 100);
        TrackedThreadSubPool subPool = new TrackedThreadSubPool(this.pool);
        makeTasksAndAwait(subPool, 50, 10);
        Assert.assertEquals(10, this.pool.getPoolSize());
    }

    @Test
    public void runManyEnd() throws ExecutionException, InterruptedException {
        this.pool = getPool(1024, 0);
        TrackedThreadSubPool subPool = new TrackedThreadSubPool(this.pool);
        makeTasksAndAwait(subPool, 2, 32);
        Thread.sleep(50);
        Assert.assertEquals(16, this.pool.getPoolSize());
    }

    @Test
    public void runManyCancel() throws ExecutionException, InterruptedException {
        this.pool = getPool(1024, 0);
        TrackedThreadSubPool subPool = new TrackedThreadSubPool(this.pool);
        List<Future> tasks = makeTasks(subPool, 200, 32);
        subPool.cancelAll();
        int cancelled = waitForTermination(tasks);
        Assert.assertEquals(32, cancelled);
    }

    @Test
    public void launchTooMany() throws ExecutionException, InterruptedException {
        // hard limit of 32 threads
        this.pool = getPool(32, 0);
        try (TrackedThreadSubPool subPool = new TrackedThreadSubPool(this.pool)) {

            // expect an exception when launching more than 32
            boolean exception = false;
            try {
                makeTasks(subPool, 100, 64);
            } catch (RejectedExecutionException ree) {
                exception = true;
            }
            Assert.assertTrue(exception);

            // ensure that only 32 threads are running
            Assert.assertTrue(this.pool.getPoolSize() == 32);
        }
    }

    private void makeTasksAndAwait(final TrackedThreadSubPool subPool, final int taskDurationMs, final int howManyTasks) throws InterruptedException, ExecutionException {
        waitForTermination(makeTasks(subPool, taskDurationMs, howManyTasks));
    }

    private int waitForTermination(List<Future> tasks) {
        int exceptions = 0;
        for (Future task : tasks) {
            try {
                task.get();
            } catch (Exception e) {
                exceptions++;
            }
        }
        return exceptions;
    }

    private ArrayList<Future> makeTasks(final TrackedThreadSubPool subPool, final int taskDurationMs, final int howManyTasks) throws InterruptedException, ExecutionException {
        ArrayList<Future> futures = new ArrayList<>();
        for (int i = 0; i < howManyTasks; i++) {
            TestThread thread = new TestThread(taskDurationMs);
            futures.add(subPool.submit(thread));
        }
        return futures;
    }

    public class TestThread implements Callable {

        int howLong;

        public TestThread(final int howLong) {
            this.howLong = howLong;
        }

        @Override
        public Object call() throws Exception {
            Thread.sleep(this.howLong);
            return null;
        }
    }

    public class TestableThreadPool extends ThreadPool {
        public TestableThreadPool(final IThreadConfig config) {
            super(config);
        }

        public int getPoolSize() {
            return ((ThreadPoolExecutor) this.getExecutorService()).getPoolSize();
        }

        public void shutdownNow() {
            getExecutorService().shutdownNow();
        }
    }

    TestableThreadPool getPool(int max, long lifespan) {
        IThreadConfig mockConfig = Mockito.mock(IThreadConfig.class);
        Mockito.when(mockConfig.getThreadPoolMaxThreads()).thenReturn(max);
        Mockito.when(mockConfig.getThreadPoolIdleTimeMs()).thenReturn(lifespan);
        return new TestableThreadPool(mockConfig);
    }


}
