package com.hutoma.api.containers.sub;

import java.util.Map;
import java.util.UUID;

public class ChatRequestInfo {
    private final UUID devId;
    private final UUID aiid;
    private final UUID chatId;
    private final String question;
    private final Map<String, String> clientVariables;
    private ChatHandoverTarget handoverTarget;

    public UUID getDevId() {
        return devId;
    }

    public UUID getAiid() {
        return aiid;
    }

    public UUID getChatId() {
        return chatId;
    }

    public String getQuestion() {
        return question;
    }

    public Map<String, String> getClientVariables() {
        return clientVariables;
    }

    public ChatHandoverTarget getHandoverTarget() {
        return handoverTarget;
    }

    public ChatRequestInfo(final UUID devId, final UUID aiid, final UUID chatId, final String question,
                           final Map<String, String> clientVariables) {
        this.devId = devId;
        this.aiid = aiid;
        this.chatId = chatId;
        this.question = question;
        this.clientVariables = clientVariables;

    }

    public ChatRequestInfo(final UUID devId, final UUID aiid, final UUID chatId, ChatHandoverTarget target) {
        this(devId, aiid, chatId, null, null);
        this.handoverTarget = target;
    }
}
