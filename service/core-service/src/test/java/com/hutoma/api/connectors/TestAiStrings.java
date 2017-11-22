package com.hutoma.api.connectors;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 09/08/17.
 */
public class TestAiStrings {

    private JsonSerializer fakeSerializer;
    private DatabaseAI fakeDatabase;
    private AiStrings aiStrings;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(DatabaseAI.class);
        this.aiStrings = new AiStrings(this.fakeDatabase, this.fakeSerializer);
    }

    private ApiAi getAi(final List<String> defaultChatResponses) {
        ApiAi ai = TestDataHelper.getAI();
        ai.setDefaultChatResponses(defaultChatResponses);
        return ai;
    }

    @Test
    public void testGetDefaultChatResponses_singleResponse() throws AiStrings.AiStringsException, DatabaseException {
        final List<String> expectedResponse = Collections.singletonList("test response");
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(getAi(expectedResponse));
        List<String> responses = this.aiStrings.getDefaultChatResponses(DEVID_UUID, AIID);
        Assert.assertEquals(expectedResponse.size(), responses.size());
        Assert.assertEquals(expectedResponse.get(0), responses.get(0));
    }

    @Test
    public void testGetDefaultChatResponses_multipleResponse() throws AiStrings.AiStringsException, DatabaseException {
        final List<String> expectedResponse = Arrays.asList("response1", "response2", "response3");
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(getAi(expectedResponse));
        List<String> responses = this.aiStrings.getDefaultChatResponses(DEVID_UUID, AIID);
        Assert.assertEquals(expectedResponse.size(), responses.size());
        Assert.assertEquals(expectedResponse.get(0), responses.get(0));
        Assert.assertEquals(expectedResponse.get(1), responses.get(1));
        Assert.assertEquals(expectedResponse.get(2), responses.get(2));
    }

    @Test()
    public void testGetDefaultChatResponses_noResponseInDb() throws AiStrings.AiStringsException, DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(getAi(Collections.emptyList()));
        Assert.assertEquals(0, this.aiStrings.getDefaultChatResponses(DEVID_UUID, AIID).size());
    }

    @Test(expected = AiStrings.AiStringsException.class)
    public void testGetDefaultChatResponses_dbException() throws AiStrings.AiStringsException, DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        this.aiStrings.getDefaultChatResponses(DEVID_UUID, AIID).size();
    }

    @Test(expected = AiStrings.AiStringsException.class)
    public void testgetRandomDefaultChatResponse_noResponseInDb() throws AiStrings.AiStringsException, DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(getAi(Collections.emptyList()));
        this.aiStrings.getRandomDefaultChatResponse(DEVID_UUID, AIID);
    }

    @Test(expected = AiStrings.AiStringsException.class)
    public void testgetRandomDefaultChatResponse_dbException() throws AiStrings.AiStringsException, DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        this.aiStrings.getRandomDefaultChatResponse(DEVID_UUID, AIID);
    }
}
