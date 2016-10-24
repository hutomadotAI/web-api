package com.hutoma.api.common;

/**
 * Created by David MG on 02/08/2016.
 */
public class FakeJsonSerializer extends JsonSerializer {

    Object unserialized;

    public Object getUnserialized() {
        return this.unserialized;
    }

    @Override
    public String serialize(Object obj) {
        this.unserialized = obj;
        return "Fake";
    }
}
