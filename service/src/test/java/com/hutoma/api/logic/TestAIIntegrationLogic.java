package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.Integration;

import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 29/10/16.
 */
public class TestAIIntegrationLogic {

    private final AIIntegrationLogic integLogic;
    private final Config fakeConfig;
    private final JsonSerializer fakeSerializer;
    private final Database fakeDatabase;
    private final ILogger fakeLogger;

    public TestAIIntegrationLogic() {
        this.fakeConfig = mock(Config.class);
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.integLogic = new AIIntegrationLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeLogger);
    }

    @Test
    public void testGetIntegrations() throws Database.DatabaseException {
        List<Integration> list = Collections.singletonList(new Integration(1, "name", "desc", "icon", true));
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(list);
        ApiIntegrationList integ = (ApiIntegrationList) this.integLogic.getIntegrations(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(list, integ.getIntegrationList());
    }

    @Test
    public void testGetIntegrations_emptyList() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(new ArrayList<>());
        ApiResult result = this.integLogic.getIntegrations(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntegrations_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.integLogic.getIntegrations(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }
}
