package com.hutoma.api.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.common.IJsonSerializer;

/**
 * Created by David MG on 02/08/2016.
 */
public class GsonSerializer implements IJsonSerializer {

    Gson gson;

    public GsonSerializer() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public String serialize(Object o) {
        return gson.toJson(o);
    }
}
