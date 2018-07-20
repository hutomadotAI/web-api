package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.chat.ChatBackendConnector;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.chat.ChatEmbHandler;
import com.hutoma.api.logic.chat.ContextVariableExtractor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        ChatResult result = new ChatResult("");
        result.setAnswer("hello $name");
        result.setContext(new HashMap<String, String>(){{put("name", "bob");}});

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello bob", result.getAnswer());
    }

    /***
     * Null context map
     */
    @Test
    public void test_Replacement_No_Context_Map() {
         ChatResult result = new ChatResult("");
        result.setAnswer("hello $name");

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Empty context map
     */
    @Test
    public void test_Replacement_Empty_Context_Map() {
        ChatResult result = new ChatResult("");
        result.setAnswer("hello $name");
        result.setContext(new HashMap<String, String>());

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Unmatched variables
     */
    @Test
    public void test_Unmatched_Variable_Replacement() {
        ChatResult result = new ChatResult("");
        result.setAnswer("hello $name");
        result.setContext(new HashMap<String, String>(){{put("surname", "bob");}});

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello $name", result.getAnswer());
    }

    /***
     * Multiple variable replacement
     */
    @Test
    public void test_Multiple_Variable_Replacement() {
        ChatResult result = new ChatResult("");
        result.setAnswer("hello $name, $name is going to $place");
        result.setContext(new HashMap<String, String>(){{put("name", "bob"); put("place", "reading");}});

        this.contextVariableExtractor.extractContextVariables(result);

        Assert.assertEquals("hello bob, bob is going to reading", result.getAnswer());
    }
}
