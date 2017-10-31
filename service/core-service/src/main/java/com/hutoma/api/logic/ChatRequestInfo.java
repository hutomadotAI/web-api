package com.hutoma.api.logic;

import java.util.Map;
import java.util.UUID;

public class ChatRequestInfo {
    public final UUID devId;
    public final UUID aiid;
    public final UUID chatId;
    public final String question;
    public final Map<String, String> clientVariables;

    public ChatRequestInfo(final UUID devId, final UUID aiid, final UUID chatId, final String question,
                    final Map<String, String> clientVariables) {
        this.devId = devId;
        this.aiid = aiid;
        this.chatId = chatId;
        this.question = question;
        this.clientVariables = clientVariables;
    }
}
