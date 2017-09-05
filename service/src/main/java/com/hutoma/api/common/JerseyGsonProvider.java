package com.hutoma.api.common;

// Teach Jersey how to use the GSON JSON serializer/deserializer
// http://memorynotfound.com/jaxrs-jersey-gson-serializer-deserializer/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JerseyGsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    private static final String PRETTY_PRINT = "pretty-print";

    private final Gson gson;
    private final Gson prettyGson;

    @Context
    private UriInfo uriInfo;

    public JerseyGsonProvider() {
        GsonBuilder builder = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization();

        this.gson = builder.create();
        this.prettyGson = builder.setPrettyPrinting().create();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations,
                      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                      InputStream entityStream) throws IOException, WebApplicationException {

        try (InputStreamReader reader = new InputStreamReader(entityStream, "UTF-8")) {
            return gson.fromJson(reader, type);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(T t, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(entityStream, "UTF-8")) {
            try (PrintWriter printWriter = new PrintWriter(outputStreamWriter)) {
                String json;
                if (uriInfo.getQueryParameters().containsKey(PRETTY_PRINT)) {
                    json = prettyGson.toJson(t);
                } else {
                    json = gson.toJson(t);
                }
                printWriter.write(json);
                printWriter.flush();
            }
        }
    }
}