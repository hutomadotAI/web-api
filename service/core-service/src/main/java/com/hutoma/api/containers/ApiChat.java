package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.Metadata;

import java.util.UUID;

/**
 * Created by David MG on 16/08/2016.
 */
public class ApiChat extends ApiResult {

    private UUID chatId;
    private long timestamp;
    private ChatResult result;
    private Metadata metadata;

    public ApiChat(UUID chatId, long timestamp) {
        this.chatId = chatId;
        this.timestamp = timestamp;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public ChatResult getResult() {
        return this.result;
    }

    public void setResult(ChatResult result) {
        this.result = result;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setID(UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
