package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logic.chat.ContextVariableExtractor;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.getSampleAI;

public class TestContextVariableExtractor extends TestChatBase {

    private ContextVariableExtractor contextVariableExtractor;

    @Before
    public void setup() {
        this.contextVariableExtractor = new ContextVariableExtractor();
    }

    /***
     * Variable replacement
     */
    @Test
    public void test_Variable_Replacement() {
        ChatResult result = getChatResultWithVars("hello $name",
                ImmutableMap.of("name", "bob"));

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello bob", result.getAnswer());
    }

    /***
     * Null context map
     */
    @Test
    public void test_Replacement_No_Context_Map() {
        ChatResult result = getChatResultWithVars("hello $name", null);

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Empty context map
     */
    @Test
    public void test_Replacement_Empty_Context_Map() {
        ChatResult result = getChatResultWithVars("hello $name", new HashMap<>());

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Unmatched variables
     */
    @Test
    public void test_Unmatched_Variable_Replacement() {
        ChatResult result = getChatResultWithVars("hello $name",
                ImmutableMap.of("surname", "bob"));

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Multiple variable replacement
     */
    @Test
    public void test_Multiple_Variable_Replacement() {
        ChatResult result = getChatResultWithVars("hello $name, $name is going to $place",
                ImmutableMap.of("name", "bob", "place", "reading"));

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello bob, bob is going to reading", result.getAnswer());
    }

    private ChatResult getChatResultWithVars(final String answer, final Map<String, String> vars) {
        ChatResult result = new ChatResult("");
        result.setAnswer(answer);
        ChatContext ctx = new ChatContext();
        if (vars != null) {
            for (Map.Entry<String, String> entry : vars.entrySet()) {
                ctx.setValue(entry.getKey(), entry.getValue(), ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
            }
        }
        result.setChatState(new ChatState(DateTime.now(), null, null, UUID.randomUUID(), new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), ctx));
        return result;
    }
}
