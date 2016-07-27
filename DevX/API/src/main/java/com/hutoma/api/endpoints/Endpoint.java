package com.hutoma.api.endpoints;

import com.google.inject.Injector;

/**
 * Created by David MG on 27/07/2016.
 */
public class Endpoint {

    protected Injector guiceInjector;

    public Endpoint() throws Exception {
        throw new Exception("Cannot instantiate a zero-parameter singleton.");
    }

    public Endpoint(Injector guiceInjector) {
        this.guiceInjector = guiceInjector;
    }
}
