package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * API entity.
 */
public class ApiEntity extends ApiResult {

    @SerializedName("entity_name")
    private final String entityName;
    @SerializedName("entity_values")
    private List<String> entityValues;
    @SerializedName("system")
    private boolean isSystem;

    /**
     * Ctor.
     * @param entityName the entity name
     */
    public ApiEntity(final String entityName) {
        this.entityName = entityName;
        this.isSystem = false;
    }

    /**
     * Ctor.
     * @param entityName the entity name
     * @param entityValues list of entity values
     * @param isSystem whether it's a system entity or not
     */
    public ApiEntity(final String entityName, final List<String> entityValues, final boolean isSystem) {
        this.entityName = entityName;
        this.entityValues = entityValues;
        this.isSystem = isSystem;
    }

    /**
     * Gets the entity name.
     * @return the entity name
     */
    public String getEntityName() {
        return this.entityName;
    }

    /**
     * Gets the list of entity values.
     * @return the list of entity values
     */
    public List<String> getEntityValueList() {
        return this.entityValues;
    }

    /**
     * Gets whether it's a system entity or not.
     * @return whether it's a system entity or not
     */
    public boolean isSystem() {
        return this.isSystem;
    }
}
