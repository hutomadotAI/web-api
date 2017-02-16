package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

public enum BackendServerType {

    @SerializedName("wnet")
    WNET,

    @SerializedName("rnn")
    RNN,

    @SerializedName("aiml")
    AIML;

}
