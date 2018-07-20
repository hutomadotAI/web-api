package com.hutoma.api.logic.chat;

import com.hutoma.api.containers.sub.ChatResult;

import java.util.Map;

public class ContextVariableExtractor {

    public void extractContextVariables(final ChatResult result) {
        if (result.getChatState() != null && result.getChatState().getChatContext() != null) {
            String response = result.getAnswer();
            for (Map.Entry<String, String> value :
                    result.getChatState().getChatContext().getVariablesAsStringMap().entrySet()) {
                response = response.replace(String.format("$%s", value.getKey()), value.getValue());
            }
            result.setAnswer(response);
        }
    }
}
