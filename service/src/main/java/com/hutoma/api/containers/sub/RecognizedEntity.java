package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import com.hutoma.api.common.JsonSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Recognized entity.
 */
public class RecognizedEntity {
    @SerializedName("category")
    private final String category;
    @SerializedName("value")
    private final String value;
    @SerializedName("end")
    private int end;
    @SerializedName("start")
    private int start;

    /**
     * Ctor.
     * @param category the entity category
     * @param value the entity value
     */
    public RecognizedEntity(final String category, final String value) {
        this.category = category;
        this.value = value;
    }

    /**
     * Deserializes the json for a list of recognized entities.
     * @param json       the json
     * @param serializer the serializer
     * @return the list of recognized entities
     */
    public static List<RecognizedEntity> deserialize(final String json, final JsonSerializer serializer) {
        List<RecognizedEntity> entities = new ArrayList<>();
        List<LinkedTreeMap> list = serializer.deserializeList(json);
        for (LinkedTreeMap tm : list) {
            if (tm.containsKey("value") && tm.containsKey("category")) {
                RecognizedEntity re = new RecognizedEntity(tm.get("category").toString(), tm.get("value").toString());
                if (tm.containsKey("start") && tm.containsKey("end")) {
                    re.start = (int) Math.round((double) tm.get("start"));
                    re.end = (int) Math.round((double) tm.get("end"));
                }
                entities.add(re);
            }
        }
        return entities;
    }

    /**
     * Gets the entity value.
     * @return the entity value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Gets the entity category.
     * @return the entity category
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Gets the start position of the entity within the string. (0-based)
     * @return the start position of the entity within the string
     */
    public int getStart() {
        return this.start;
    }

    /**
     * Gets the end position of the entity within the string. (0-based)
     * @return the end position of the entity within the string
     */
    public int getEnd() {
        return this.end;
    }
}
