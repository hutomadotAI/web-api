package com.hutoma.api.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by David MG on 02/08/2016.
 */
public class JsonSerializer {

    Gson gson;

    public JsonSerializer() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String serialize(Object o) {
        return gson.toJson(o);
    }
}
