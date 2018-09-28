package com.hutoma.api.common;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JSON serializer.
 */
public class JsonSerializer {

    private Gson gson;
    private final boolean enablePrettyPrinting;

    public JsonSerializer() {
        this(false);
    }

    public JsonSerializer(final boolean prettyPrinting) {
        this.enablePrettyPrinting = prettyPrinting;
        this.gson = createGsonBuilder(prettyPrinting, false).create();
    }

    public void allowNullsOnSerialization() {
        this.gson = createGsonBuilder(this.enablePrettyPrinting, true).create();
    }

    /**
     * Serializes an object into Json
     * @param obj the object to serialize
     * @return the json string
     */
    public String serialize(Object obj) {
        return this.gson.toJson(obj);
    }

    /**
     * Deserializes a stream into an object.
     * @param stream      the stream
     * @param resultClass the type expected to deserialize
     * @return the deserialized object
     * @throws JsonParseException when there's an error parsing the input object
     */
    @SuppressWarnings("unchecked")
    public Object deserialize(final InputStream stream, final Class resultClass) throws JsonParseException {
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

    /**
     * Deserializes a string into an object.
     * @param content     the string content
     * @param resultClass the type expected to deserialize
     * @return the deserialized object
     * @throws JsonParseException when there's an error parsing the input object
     */
    @SuppressWarnings("unchecked")
    public Object deserialize(final String content, final Class resultClass) throws JsonParseException {
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

    /**
     * Deserializes a list into an object, auto-detecting the type (only works for simple types)
     * @param content the json content
     * @return the deserialized object
     * @throws JsonParseException when there's an error parsing the input object
     */
    public <T> List<T> deserializeListAutoDetect(final String content) throws JsonParseException {
        try {
            if (content == null) {
                return Collections.emptyList();
            }
            Type listType = new TypeToken<List<T>>() {
            }.getType();
            List<T> list = this.gson.fromJson(content, listType);
            if (list == null) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return list;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    /**
     * Deserializes a list into an object.
     * @param content  the json content
     * @param listType the list type expected to deserialize
     * @return the deserialized object
     * @throws JsonParseException when there's an error parsing the input object
     */
    public <T> List<T> deserializeList(final String content, final Type listType) throws JsonParseException {
        try {
            if (content == null) {
                return Collections.emptyList();
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

    /**
     * Deserializes a Map where both keys and values are strings.
     * @param content the json content
     * @return the Map
     * @throws JsonParseException
     */
    public Map<String, String> deserializeStringMap(final String content) throws JsonParseException {
        try {
            if (content == null || content.isEmpty()) {
                return new HashMap<>();
            }
            Map<String, String> stringMap = this.gson.fromJson(content, new TypeToken<Map<String, String>>() {
            }.getType());
            if (stringMap == null) {
                throw new JsonParseException("cannot deserialize valid object from json");
            }
            return stringMap;
        } catch (JsonSyntaxException jse) {
            throw new JsonParseException(jse);
        }
    }

    /**
     * Deserializes a Map where both keys and values are strings.
     * @param content the json content
     * @return the Map
     * @throws JsonParseException
     */
    public Map<String, String> deserializeStringMap(final InputStream content) throws JsonParseException {
        try {
            return this.deserializeStringMap(IOUtils.toString(content, StandardCharsets.UTF_8.name()));
        } catch (JsonSyntaxException | IOException ex) {
            throw new JsonParseException(ex);
        }
    }

    private GsonBuilder createGsonBuilder(final boolean prettyPrinting, final boolean includeNulls) {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(Locale.class, new LocaleTypeAdapter())
                .registerTypeAdapter(SupportedLanguage.class, new SupportedLanguageAdapter())
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

    public static class SupportedLanguageAdapter extends  TypeAdapter<SupportedLanguage> {
        @Override
        public void write(JsonWriter writer, SupportedLanguage value) throws IOException {
            if (value == null) {
                writer.nullValue();
                return;
            }
            String supportedLanguage = value.toString().toLowerCase();
            writer.value(supportedLanguage);
        }

        @Override
        public SupportedLanguage read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return SupportedLanguage.EN;
            }
            String supportedLanguage = reader.nextString();
            return SupportedLanguage.get(supportedLanguage);
        }
    }
}
