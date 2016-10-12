package com.hutoma.api.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by David MG on 02/08/2016.
 */
public class JsonSerializer {

    private Gson gson;

    public JsonSerializer() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(DateTime.class, new com.google.gson.JsonSerializer<DateTime>() {
                @Override
                public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                }
            })
            .create();
    }

    public String serialize(Object o) {
        return this.gson.toJson(o);
    }

    public Object deserialize(InputStream stream, Class resultClass) throws JsonParseException {
        Object o = this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), resultClass);
        if (null == o) {
            throw new JsonParseException("cannot deserialize valid object from json");
        }
        return o;
    }

    public Object deserialize(String content, Class resultClass) throws JsonParseException {
        Object o = this.gson.fromJson(content, resultClass);
        if (null == o) {
            throw new JsonParseException("cannot deserialize valid object from json");
        }
        return o;
    }

    public <T> List<T> deserializeList(String content) throws JsonParseException {
        List<T> list = gson.fromJson(content, new TypeToken<List<T>>(){}.getType());
        if (list == null) {
            throw new JsonParseException("cannot deserialize valid object from json");
        }
        return list;
    }
}
