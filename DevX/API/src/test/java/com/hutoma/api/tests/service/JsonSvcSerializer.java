package com.hutoma.api.tests.service;

/**
 * Created by pedrotei on 30/10/16.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Created by David MG on 02/08/2016.
 */
class JsonSvcSerializer {

    private final Gson gson;

    JsonSvcSerializer() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(DateTime.class, new com.google.gson.JsonSerializer<DateTime>() {
                    @Override
                    public JsonElement serialize(DateTime json, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(ISODateTimeFormat.dateTime().print(json));
                    }
                })
                .registerTypeAdapter(DateTime.class, new com.google.gson.JsonDeserializer<DateTime>() {
                    @Override
                    public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                            throws JsonParseException {
                        return new DateTime(json.getAsString());
                    }
                })
                .create();
    }


    Object deserialize(InputStream stream, Class resultClass) throws JsonSyntaxException {
        Object obj = this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), resultClass);
        if (null == obj) {
            throw new JsonParseException("cannot deserialize valid object from json");
        }
        return obj;
    }
}

