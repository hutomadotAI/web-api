package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public enum IntentConditionOperator {
    @SerializedName("SET")
    SET,

    @SerializedName("NOT_SET")
    NOT_SET,

    @SerializedName("EQUALS")
    EQUALS,

    @SerializedName("NOT_EQUALS")
    NOT_EQUALS,

    @SerializedName("SMALLER_THAN")
    SMALLER_THAN,

    @SerializedName("GREATER_THAN")
    GREATER_THAN;
}