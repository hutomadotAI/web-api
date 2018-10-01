package com.hutoma.api.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IThreadSubPool {
    Future submit(Runnable runnable);

    Future submit(Callable callable);
}
