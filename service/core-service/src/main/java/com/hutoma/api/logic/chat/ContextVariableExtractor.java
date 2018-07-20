package com.hutoma.api.logic.chat;

import com.hutoma.api.containers.sub.ChatResult;

import java.util.Map;

public class ContextVariableExtractor {

    public void extractContextVariables(final ChatResult result) {
        if (result.getContext() != null) {
            if (!result.getContext().isEmpty()) {
                String response = result.getAnswer();
                for (Map.Entry<String, String> value : result.getContext().entrySet()) {
                    response = response.replace(String.format("$%s", value.getKey()), value.getValue());
                }
                result.setAnswer(response);
            }
        }
    }
}
