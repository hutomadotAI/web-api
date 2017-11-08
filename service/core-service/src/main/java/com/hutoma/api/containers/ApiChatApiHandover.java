package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.ChatHandoverTarget;

import java.util.UUID;

public class ApiChatApiHandover extends ApiResult {

    @SerializedName("chatId")
    private UUID chatId;
    @SerializedName("target")
    private String target;

    public ApiChatApiHandover(final UUID chatId, final ChatHandoverTarget target) {
        this.chatId = chatId;
        this.target = target.getStringValue();
    }
}
