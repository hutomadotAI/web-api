package com.hutoma.api.containers;

import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.WebHookResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

public class TestChatResult {

    @Test
    public void testChatResult_getUserViewable() {
        final UUID chatId = UUID.randomUUID();
        final UUID aiid = UUID.randomUUID();
        final double score = 0.12345;
        final String query = "the query?";
        final String answer = "the answer";
        final double elapsedTime = 1.3579;
        final String intentName = "intentName";
        final String webhookResponseString = null;
        MemoryVariable memoryVariable = new MemoryVariable("varName", Collections.singletonList("value1"));
        MemoryIntent intent = new MemoryIntent(intentName, aiid, chatId, Collections.singletonList(memoryVariable));
        WebHookResponse webhookResponse = new WebHookResponse(webhookResponseString);
        ChatResult result = new ChatResult(chatId, score, query, answer, elapsedTime, webhookResponse);
        result.setIntents(Collections.singletonList(intent));
        ChatResult userViewable = ChatResult.getUserViewable(result);
        Assert.assertEquals(chatId, userViewable.getChatId());
        Assert.assertEquals(score, userViewable.getScore(), 0.00001);
        Assert.assertEquals(query, userViewable.getQuery());
        Assert.assertEquals(answer, userViewable.getAnswer());
        Assert.assertEquals(elapsedTime, userViewable.getElapsedTime(), 0.00001);
        Assert.assertEquals(1, userViewable.getIntents().size());
        Assert.assertEquals(intent.getName(), userViewable.getIntents().get(0).getName());
        Assert.assertEquals(intent.getVariables().size(), userViewable.getIntents().get(0).getVariables().size());
        Assert.assertEquals(intent.getVariables().get(0).getName(), userViewable.getIntents().get(0).getVariables().get(0).getName());
        Assert.assertEquals(webhookResponseString, userViewable.getWebhookResponse().getText());
    }
}
