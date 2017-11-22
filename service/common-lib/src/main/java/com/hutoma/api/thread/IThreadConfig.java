package com.hutoma.api.thread;

public interface IThreadConfig {
    /***
     * The maximum number of active threads in the threadpool
     * after which anyone requesting a thread will get an exception
     * @return
     */
    int getThreadPoolMaxThreads();

    /***
     * The time after which an idle thread in the thread pool get be closed
     * @return
     */
    long getThreadPoolIdleTimeMs();
}
