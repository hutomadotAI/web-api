package com.hutoma.api.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ITrackedThreadSubPool extends IThreadSubPool {
    void cancelAll();
}
