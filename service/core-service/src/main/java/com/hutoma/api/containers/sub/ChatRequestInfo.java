package com.hutoma.api.containers.sub;

import java.util.Map;
import java.util.UUID;

public class ChatRequestInfo {
    private final AiIdentity aiIdentity;
    private final UUID chatId;
    private String question;
    private final Map<String, String> clientVariables;
    private ChatHandoverTarget handoverTarget;

    public UUID getDevId() {
        return this.aiIdentity.getDevId();
    }

    public UUID getAiid() {
        return this.aiIdentity.getAiid();
    }

    public AiIdentity getAiIdentity() {
        return this.aiIdentity;
    }

    public UUID getChatId() {
        return chatId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(final String q) {
        this.question = q;
    }

    public Map<String, String> getClientVariables() {
        return clientVariables;
    }

    public ChatHandoverTarget getHandoverTarget() {
        return handoverTarget;
    }

    public ChatRequestInfo(final AiIdentity aiIdentity,
                           final UUID chatId,
                           final String question,
                           final Map<String, String> clientVariables) {
        this.aiIdentity = aiIdentity;
        this.chatId = chatId;
        this.question = question;
        this.clientVariables = clientVariables;

    }

    public ChatRequestInfo(final AiIdentity aiIdentity, final UUID chatId, ChatHandoverTarget target) {
        this(aiIdentity, chatId, null, null);
        this.handoverTarget = target;
    }
}
