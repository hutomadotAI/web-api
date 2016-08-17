package com.hutoma.api.common;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

/**
 * Created by David MG on 02/08/2016.
 */
public class JsonSerializer {

    Gson gson;

    public JsonSerializer() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(DateTime.class, new com.google.gson.JsonSerializer<DateTime>(){
                    @Override
                    public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                    }
                })
                .create();
    }

    public String serialize(Object o) {
        return gson.toJson(o);
    }
}
