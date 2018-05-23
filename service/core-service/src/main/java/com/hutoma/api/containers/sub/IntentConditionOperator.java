package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public enum IntentConditionOperator {
    @SerializedName("set")
    SET,

    @SerializedName("!set")
    NOT_SET,

    @SerializedName("==")
    EQUALS,

    @SerializedName("!=")
    NOT_EQUALS,

    @SerializedName("<")
    BIGGER_THAN,

    @SerializedName(">")
    SMALLER_THAN;
}