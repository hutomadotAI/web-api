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
    GREATER_THAN,

    @SerializedName("SMALLER_THAN_OR_EQUALS")
    SMALLER_THAN_OR_EQUALS,

    @SerializedName("GREATER_THAN_OR_EQUALS")
    GREATER_THAN_OR_EQUALS;
}