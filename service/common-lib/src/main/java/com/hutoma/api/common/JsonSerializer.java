package com.hutoma.api.common;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * JSON serializer.
 */
public class JsonSerializer {

    private Gson gson;
    private final boolean enablePrettyPrinting;

    public JsonSerializer() {
        this(true);
    }

    public JsonSerializer(final boolean prettyPrinting) {
        this.enablePrettyPrinting = prettyPrinting;
        this.gson = createGsonBuilder(prettyPrinting, false).create();
    }

    public void allowNullsOnSerialization() {
        this.gson = createGsonBuilder(this.enablePrettyPrinting, true).create();
    }

    public String serialize(Object obj) {
        return this.gson.toJson(obj);
    }

    @SuppressWarnings("unchecked")
    public Object deserialize(InputStream stream, Class resultClass) throws JsonParseException {
        try {
            Object obj = this.gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), resultClass);
            if (null == obj) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return obj;
        } catch (NumberFormatException | JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    @SuppressWarnings("unchecked")
    public Object deserialize(String content, Class resultClass) throws JsonParseException {
        try {
            if (content == null) {
                return null;
            }
            Object obj = this.gson.fromJson(content, resultClass);
            if (null == obj) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return obj;
        } catch (NumberFormatException | JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    public <T> List<T> deserializeListAutoDetect(String content) throws JsonParseException {
        try {
            if (content == null) {
                return null;
            }
            Type listType = new TypeToken<List<T>>(){}.getType();
            List<T> list = this.gson.fromJson(content, listType);
            if (list == null) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return list;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    public <T> List<T> deserializeList(String content, Type listType) throws JsonParseException {
        try {
            if (content == null) {
                return null;
            }
            List<T> list = this.gson.fromJson(content, listType);
            if (list == null) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return list;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    private GsonBuilder createGsonBuilder(final boolean prettyPrinting, final boolean includeNulls) {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(Locale.class, new LocaleTypeAdapter())
                .enableComplexMapKeySerialization();
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
        if (includeNulls) {
            builder.serializeNulls();
        }
        return builder;
    }

    public static class DateTimeSerializer implements JsonDeserializer<DateTime>,
            com.google.gson.JsonSerializer<DateTime> {

        private static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

        @Override
        public DateTime deserialize(final JsonElement je, final Type type,
                                    final JsonDeserializationContext jdc) throws JsonParseException {
            final String dateAsString = je.getAsString();
            return dateAsString.length() == 0 ? null : DATE_FORMAT.parseDateTime(dateAsString);
        }

        @Override
        public JsonElement serialize(final DateTime src, final Type typeOfSrc,
                                     final JsonSerializationContext context) {
            return new JsonPrimitive(src == null ? "" : DATE_FORMAT.print(src));
        }
    }

    public static class LocaleTypeAdapter extends TypeAdapter<Locale> {
        @Override
        public void write(JsonWriter writer, Locale value) throws IOException {
            if (value == null) {
                writer.nullValue();
                return;
            }
            String localeString = value.toLanguageTag();
            writer.value(localeString);
        }

        @Override
        public Locale read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return null;
            }
            String localeString = reader.nextString();
            return Locale.forLanguageTag(localeString);
        }
    }
}
