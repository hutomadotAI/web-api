package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;

/**
 * Created by David MG on 02/08/2016.
 */
public class FakeJsonSerializer extends JsonSerializer {

    Object unserialized;

    public Object getUnserialized() {
        return unserialized;
    }

    @Override
    public String serialize(Object o) {
        unserialized = o;
        return "Fake";
    }
}
