package com.hutoma.api.common;

import com.google.inject.Provider;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by David MG on 13/10/2016.
 */
public class FakeProvider<T> implements Provider<T> {

    private Queue<T> fakes;

    public FakeProvider(List<T> listOfFakes) {
        this.fakes = new LinkedList<T>(listOfFakes);
    }

    @Override
    public T get() {
        return this.fakes.remove();
    }
}