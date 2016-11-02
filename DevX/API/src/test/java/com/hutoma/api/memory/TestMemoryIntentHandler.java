package com.hutoma.api.memory;

import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static junitparams.JUnitParamsRunner.$;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 06/10/16.
 */
@RunWith(JUnitParamsRunner.class)
public class TestMemoryIntentHandler {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final UUID CHATID = UUID.fromString("cee37b17-8cb3-4678-b8ba-91924eb98272");
    private static final String INTENT_NAME = "intent1";
    private static final String DEFAULT_INTENT = "@meta.intent." + INTENT_NAME;

    private MemoryIntentHandler memoryIntentHandler;
    private FakeJsonSerializer fakeSerializer;
    private Database fakeDatabase;
    private DatabaseEntitiesIntents fakeDatabaseEntities;
    private Logger fakeLogger;
    private IEntityRecognizer fakeRecognizer;

    private static Object[] recognizeIntentDataProvider() {
        return $(
                $("@meta.intent." + INTENT_NAME),
                $("@meta.intent." + INTENT_NAME + " "),
                $("@meta.intent." + INTENT_NAME + " this is something else"),
                $("@meta.intent." + INTENT_NAME + " line1\nline2")
        );
    }

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(Logger.class);
        this.fakeRecognizer = mock(IEntityRecognizer.class);
        this.fakeDatabaseEntities = mock(DatabaseEntitiesIntents.class);
        this.memoryIntentHandler = new MemoryIntentHandler(this.fakeSerializer, this.fakeDatabaseEntities, this.fakeLogger,
                this.fakeDatabase);
    }

    @Test
    public void testGetIntent() throws Database.DatabaseException {
        ApiIntent intent = new ApiIntent(INTENT_NAME, "", "");
        when(this.fakeDatabaseEntities.getIntent(any(), any(), any())).thenReturn(intent);
        ApiIntent result = this.memoryIntentHandler.getIntent(DEVID, AIID, INTENT_NAME);
        Assert.assertEquals(intent.getIntentName(), result.getIntentName());
    }

    @Test
    public void testGetIntent_dbException() throws Database.DatabaseException {
        when(this.fakeDatabaseEntities.getIntent(any(), any(), any())).thenThrow(Database.DatabaseException.class);
        Assert.assertNull(this.memoryIntentHandler.getIntent(DEVID, AIID, INTENT_NAME));
    }

    @Test
    @Parameters(method = "recognizeIntentDataProvider")
    public void testRecognizeIntent(String response) throws Database.DatabaseException {
        MemoryIntent mi = setDummyMemoryIntent(response);
        Assert.assertNotNull(mi);
        Assert.assertEquals(INTENT_NAME, mi.getName());
        Assert.assertEquals(CHATID, mi.getChatId());
    }

    @Test
    public void testRecognizeNoIntent() {
        MemoryIntent mi = this.memoryIntentHandler.parseAiResponseForIntent(DEVID, AIID, CHATID, "@this is not");
        Assert.assertNull(mi);
    }

    @Test
    public void testIntentVariableFulfilled() throws Database.DatabaseException {
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
    public void testIntentUpdate() throws Database.DatabaseException {
        MemoryIntent mi = this.setDummyMemoryIntent("");
        this.memoryIntentHandler.updateStatus(mi);
        try {
            // This seems to count once for the invocation above and another for this
            verify(this.fakeDatabase, atMost(2)).updateMemoryIntent(mi, this.fakeSerializer);
        } catch (Database.DatabaseException dbe) {
            Assert.fail(dbe.getMessage());
        }
    }

    @Test
    public void testMemoryIntentULoadNotExistYet() throws Database.DatabaseException {
        final String entityName = "entity1";
        ApiIntent apiIntent = new ApiIntent(INTENT_NAME, "in", "out");
        IntentVariable iv = new IntentVariable(entityName, true, 1, null);
        ApiEntity apiEntity = new ApiEntity(entityName, Arrays.asList("a", "b"));
        apiIntent.addVariable(iv);
        when(this.fakeDatabaseEntities.getIntent(anyString(), any(), anyString())).thenReturn(apiIntent);
        when(this.fakeDatabase.getMemoryIntent(anyString(), any(), any(), any())).thenReturn(null);
        when(this.fakeDatabaseEntities.getEntity(anyString(), anyString())).thenReturn(apiEntity);
        MemoryIntent mi = this.memoryIntentHandler.parseAiResponseForIntent(DEVID, AIID, CHATID, DEFAULT_INTENT);
        Assert.assertNotNull(mi.getVariables());
        Assert.assertEquals(1, mi.getVariables().size());
        Assert.assertEquals(entityName, mi.getVariables().get(0).getName());
        Assert.assertNull(mi.getVariables().get(0).getCurrentValue());
    }

    @Test()
    public void testIntentUpdateDBException() throws Database.DatabaseException {
        MemoryIntent mi = this.setDummyMemoryIntent("");
        Database.DatabaseException exception = new Database.DatabaseException(new Throwable());
        when(this.fakeDatabase.updateMemoryIntent(any(), any())).thenThrow(exception);
        this.memoryIntentHandler.updateStatus(mi);
        verify(this.fakeLogger).logError(anyString(), anyString());
    }

    @Test()
    public void testGetCurrentStateForChatDBException() throws Database.DatabaseException {
        Database.DatabaseException exception = new Database.DatabaseException(new Throwable());
        when(this.fakeDatabase.getMemoryIntentsForChat(any(), any(), any())).thenThrow(exception);
        this.memoryIntentHandler.getCurrentIntentsStateForChat(AIID, CHATID);
        verify(this.fakeLogger).logError(anyString(), anyString());
    }

    @Test
    public void testLoadIntentForAiDBException() throws Database.DatabaseException {
        Database.DatabaseException exception = new Database.DatabaseException(new Throwable());
        when(this.fakeDatabase.getMemoryIntent(anyString(), any(), any(), any())).thenThrow(exception);
        this.memoryIntentHandler.parseAiResponseForIntent(DEVID, AIID, CHATID, DEFAULT_INTENT);
        verify(this.fakeLogger).logError(anyString(), anyString());
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
                5);
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

    @Test
    public void testIntentDeleteAllAIIntents() throws Database.DatabaseException {
        this.memoryIntentHandler.deleteAllIntentsForAi(AIID);
        verify(this.fakeDatabase).deleteAllMemoryIntents(AIID);
    }

    @Test
    public void testIntentDeleteAllAIIntentsDbException() throws Database.DatabaseException {
        Database.DatabaseException exception = new Database.DatabaseException(new Throwable());
        when(this.fakeDatabase.deleteAllMemoryIntents(any())).thenThrow(exception);
        this.memoryIntentHandler.deleteAllIntentsForAi(AIID);
        verify(this.fakeLogger).logError(anyString(), anyString());
    }

    private MemoryIntent setDummyMemoryIntent(final String response) throws Database.DatabaseException {
        MemoryIntent mi = new MemoryIntent(INTENT_NAME, AIID, CHATID,
                Collections.singletonList(new MemoryVariable("name", Arrays.asList("a", "b", "c"))));
        when(this.fakeDatabase.getMemoryIntent(any(), any(), any(), any())).thenReturn(mi);
        return this.memoryIntentHandler.parseAiResponseForIntent(DEVID, AIID, CHATID, response);
    }
}
