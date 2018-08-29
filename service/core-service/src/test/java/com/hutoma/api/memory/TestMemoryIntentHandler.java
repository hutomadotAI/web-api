package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.ChatContext;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.ChatLogic;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.getSampleAI;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 06/10/16.
 */
@RunWith(DataProviderRunner.class)
public class TestMemoryIntentHandler {
    public static final UUID DEVID_UUID = UUID.fromString("113d39cb-7f43-40d7-8dee-17b25b205581");
    public static final String DEVID = DEVID_UUID.toString();
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final UUID CHATID = UUID.fromString("cee37b17-8cb3-4678-b8ba-91924eb98272");
    private static final String INTENT_NAME = "intent1";
    private static final String DEFAULT_INTENT = MemoryIntentHandler.META_INTENT_TAG + INTENT_NAME;

    private MemoryIntentHandler memoryIntentHandler;
    private JsonSerializer fakeSerializer;
    private Database fakeDatabase;
    private DatabaseEntitiesIntents fakeDatabaseEntities;
    private ILogger fakeLogger;
    private IEntityRecognizer fakeRecognizer;

    @DataProvider
    public static Object[] recognizeIntentDataProvider() {
        return new Object[]{
                MemoryIntentHandler.META_INTENT_TAG + INTENT_NAME,
                MemoryIntentHandler.META_INTENT_TAG + INTENT_NAME + " ",
                MemoryIntentHandler.META_INTENT_TAG + INTENT_NAME + " this is something else",
                MemoryIntentHandler.META_INTENT_TAG + INTENT_NAME + " line1\nline2"
        };
    }

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeDatabaseEntities = mock(DatabaseEntitiesIntents.class);
        this.memoryIntentHandler = new MemoryIntentHandler(this.fakeSerializer, this.fakeDatabaseEntities, this.fakeLogger,
                this.fakeDatabase);
    }

    @Test
    public void testGetIntent() throws DatabaseException {
        ApiIntent intent = new ApiIntent(INTENT_NAME, "", "");
        when(this.fakeDatabaseEntities.getIntent(any(), any())).thenReturn(intent);
        ApiIntent result = this.memoryIntentHandler.getIntent(AIID, INTENT_NAME);
        Assert.assertEquals(intent.getIntentName(), result.getIntentName());
    }

    @Test
    public void testGetIntent_dbException() throws DatabaseException {
        when(this.fakeDatabaseEntities.getIntent(any(), any())).thenThrow(DatabaseException.class);
        Assert.assertNull(this.memoryIntentHandler.getIntent(AIID, INTENT_NAME));
    }

    @Test
    @UseDataProvider("recognizeIntentDataProvider")
    public void testRecognizeIntent(String response) throws DatabaseException, ChatLogic.IntentException {
        MemoryIntent mi = setDummyMemoryIntent(response);
        Assert.assertNotNull(mi);
        Assert.assertEquals(INTENT_NAME, mi.getName());
        Assert.assertEquals(CHATID, mi.getChatId());
    }

    @Test
    public void testRecognizeNoIntent() throws ChatLogic.IntentException {
        MemoryIntent mi = this.memoryIntentHandler.parseAiResponseForIntent(DEVID_UUID, AIID, CHATID, "@this is not", buildChatState());
        Assert.assertNull(mi);
    }

    @Test(expected = ChatLogic.IntentException.class)
    public void testRecognize_UnknownIntent() throws ChatLogic.IntentException {
        this.memoryIntentHandler.parseAiResponseForIntent(DEVID_UUID, AIID, CHATID, "@meta.intent.unknown", buildChatState());
    }

    @Test
    public void testIntentVariableFulfilled() {
        final String entityName = "name";
        final String entityValue = "a";
        MemoryIntent mi = new MemoryIntent(INTENT_NAME, AIID, CHATID,
                Collections.singletonList(
                        new MemoryVariable(entityName, Arrays.asList(entityValue, "b", "c"))));
        List<Pair<String, String>> entities = Collections.singletonList(new Pair<>(entityName, entityValue));
        mi.fulfillVariables(entities);
        Assert.assertEquals(0, mi.getUnfulfilledVariables().size());
    }

    @Test
    public void testMemoryIntentULoadNotExistYet() throws ChatLogic.IntentException, DatabaseException {
        final String entityName = "entity1";
        ApiIntent apiIntent = new ApiIntent(INTENT_NAME, "in", "out");
        IntentVariable iv = new IntentVariable(entityName, DEVID_UUID, true, 1, null, false, "", false);
        ApiEntity apiEntity = new ApiEntity(entityName, DEVID_UUID, Arrays.asList("a", "b"), false);
        apiIntent.addVariable(iv);
        when(this.fakeDatabaseEntities.getIntent(any(), anyString())).thenReturn(apiIntent);
        when(this.fakeDatabaseEntities.getEntity(any(), anyString())).thenReturn(apiEntity);
        MemoryIntent mi = this.memoryIntentHandler.parseAiResponseForIntent(DEVID_UUID, AIID, CHATID, DEFAULT_INTENT, buildChatState());
        Assert.assertNotNull(mi.getVariables());
        Assert.assertEquals(1, mi.getVariables().size());
        Assert.assertEquals(entityName, mi.getVariables().get(0).getName());
        Assert.assertNull(mi.getVariables().get(0).getCurrentValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadIntentForAi_duplicateLabels_throwsException() {
        final String label = "theLabel";
        MemoryVariable var1 = new MemoryVariable("var1", "val", true, Collections.singletonList("sys.test"),
                Collections.singletonList("Prompt"), 1, 0, true, false, label, false);
        MemoryVariable var2 = new MemoryVariable("var2", "val", true, Collections.singletonList("sys.test"),
                Collections.singletonList("Prompt"), 1, 0, true, false, label, false);
        new MemoryIntent(INTENT_NAME, AIID, CHATID, Arrays.asList(var1, var2));
    }

    @Test
    public void testMemoryIntentCtor() {
        MemoryVariable mv = new MemoryVariable("name", null);
        List<MemoryVariable> mvl = Collections.singletonList(mv);
        MemoryIntent mi = new MemoryIntent(INTENT_NAME, AIID, CHATID, mvl);
        Assert.assertEquals(INTENT_NAME, mi.getName());
        Assert.assertEquals(AIID, mi.getAiid());
        Assert.assertEquals(CHATID, mi.getChatId());
        Assert.assertEquals(mvl, mi.getVariables());
        Assert.assertEquals(new ArrayList<MemoryVariable>(), mi.getUnfulfilledVariables());
        Assert.assertEquals(mv, mi.getVariables().get(0));
    }

    @Test
    public void testMemoryIntentFulfillProperty() {
        MemoryIntent mi = new MemoryIntent(INTENT_NAME, AIID, CHATID, null);
        mi.setIsFulfilled(true);
        Assert.assertEquals(true, mi.isFulfilled());
        mi.setIsFulfilled(false);
        Assert.assertEquals(false, mi.isFulfilled());
    }

    @Test
    public void testMemoryVariableProperties() {
        List<String> values = Arrays.asList("a", "b", "c");
        MemoryVariable mv = new MemoryVariable(
                "name",
                "currentValue",
                true,
                values,
                Collections.singletonList("prompt"),
                123,
                5,
                false,
                false,
                "label",
                false);
        Assert.assertEquals("name", mv.getName());
        Assert.assertEquals("currentValue", mv.getCurrentValue());
        Assert.assertEquals(values, mv.getEntityKeys());
        Assert.assertEquals(123, mv.getTimesToPrompt());
        Assert.assertEquals(5, mv.getTimesPrompted());
        Assert.assertEquals(true, mv.isMandatory());
        Assert.assertEquals("prompt", mv.getPrompts().get(0));

        mv.setName("newName");
        values = Arrays.asList("x", "y");
        mv.setEntityKeys(values);
        Assert.assertEquals("newName", mv.getName());
        Assert.assertEquals(values, mv.getEntityKeys());
    }

    private MemoryIntent setDummyMemoryIntent(final String response)
            throws ChatLogic.IntentException {
        MemoryIntent mi = new MemoryIntent(INTENT_NAME, AIID, CHATID,
                Collections.singletonList(new MemoryVariable("name", Arrays.asList("a", "b", "c"))));
        ChatState state = buildChatState();
        state.setCurrentIntents(Collections.singletonList(mi));

        return this.memoryIntentHandler.parseAiResponseForIntent(DEVID_UUID, AIID, CHATID, response, state);
    }

    private ChatState buildChatState() {
        return new ChatState(DateTime.now(), "theTopic", "theHistory", AIID, new HashMap<>(), 0.5d,
                ChatHandoverTarget.Ai, getSampleAI(), new ChatContext());
    }
}
