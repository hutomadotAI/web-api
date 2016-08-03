package com.hutoma.api.logic;

import com.hutoma.api.common.IJsonSerializer;

/**
 * Created by David MG on 02/08/2016.
 */
public class FakeJsonSerializer implements IJsonSerializer {

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
