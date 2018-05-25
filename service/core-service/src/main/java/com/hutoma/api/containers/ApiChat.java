package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.Metadata;

import java.util.UUID;

/**
 * API Response for a chat call.
 */
public class ApiChat extends ApiResult {

    @SerializedName("chatId")
    private UUID chatId;
    @SerializedName("timestamp")
    private long timestamp;
    @SerializedName("result")
    private ChatResult result;
    @SerializedName("metadata")
    private Metadata metadata;

    /**
     * Ctor.
     * @param chatId the chat session id
     * @param timestamp the timestamp
     */
    public ApiChat(UUID chatId, long timestamp) {
        this.chatId = chatId;
        this.timestamp = timestamp;
    }

    /**
     * Sets the metadata.
     * @param metadata the metadata
     */
    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the chat result.
     * @return the caht result
     */
    public ChatResult getResult() {
        return this.result;
    }

    /**
     * Sets the chat result.
     * @param result the chat result
     */
    public void setResult(final ChatResult result) {
        this.result = result;
    }

    /**
     * Sets the chat session id.
     * @param chatId the chat session id
     */
    public void setID(final UUID chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the chat session id.
     * @return the chat session id
     */
    public UUID getChatId() {
        return chatId;
    }

    /**
     * Gets the timestamp.
     * @return the timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }
}
