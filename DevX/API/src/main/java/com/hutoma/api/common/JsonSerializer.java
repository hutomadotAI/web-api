package com.hutoma.api.common;

import com.google.gson.*;
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
        try {
            Object o = this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), resultClass);
            if (null == o) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return o;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    public Object deserialize(String content, Class resultClass) throws JsonParseException {
        try {
            Object o = this.gson.fromJson(content, resultClass);
            if (null == o) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return o;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    public <T> List<T> deserializeList(String content) throws JsonParseException {
        try {
            List<T> list = this.gson.fromJson(content, new TypeToken<List<T>>() {
            }.getType());
            if (list == null) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return list;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }
}
