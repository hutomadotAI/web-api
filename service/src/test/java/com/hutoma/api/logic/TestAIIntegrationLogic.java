package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.FacebookConnector;
import com.hutoma.api.connectors.FacebookException;
import com.hutoma.api.containers.ApiFacebookIntegration;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookIntegrationMetadata;
import com.hutoma.api.containers.facebook.FacebookNode;
import com.hutoma.api.containers.facebook.FacebookNodeList;
import com.hutoma.api.containers.facebook.FacebookToken;
import com.hutoma.api.containers.sub.Integration;
import com.hutoma.api.containers.sub.IntegrationRecord;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestAIIntegrationLogic {

    private AIIntegrationLogic integLogic;
    private Config fakeConfig;
    private JsonSerializer serializer;
    private Database fakeDatabase;
    private ILogger fakeLogger;
    private FacebookConnector fakeConnector;
    private FacebookToken fakeToken;
    private FacebookNode fakeNode;
    private IntegrationRecord fakeIntegrationRecord;

    @Before
    public void setup() throws FacebookException, Database.DatabaseException {
        this.fakeConfig = mock(Config.class);
        this.serializer = new JsonSerializer();
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeConnector = mock(FacebookConnector.class);

        this.fakeNode = mock(FacebookNode.class);
        this.fakeToken = mock(FacebookToken.class);
        when(this.fakeConnector.getFacebookUserToken(any())).thenReturn(this.fakeToken);
        when(this.fakeConnector.getFacebookUserFromToken(any())).thenReturn(this.fakeNode);

        this.fakeIntegrationRecord = mock(IntegrationRecord.class);
        when(this.fakeDatabase.getIntegration(any(), any(), any())).thenReturn(this.fakeIntegrationRecord);

        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        metadata.setPageToken("validpagetoken");
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        when(this.fakeIntegrationRecord.getIntegrationUserid()).thenReturn("userid");
        when(this.fakeConnector.getUserPages(anyString())).thenReturn(
                new FacebookNodeList(Collections.singletonList(
                        new FacebookNode(TestDataHelper.ALT_SESSIONID.toString(), "pagename",
                                Collections.singletonList("perm"), "token"))));

        when(this.fakeConnector.getUserGrantedPermissions(any(), any()))
                .thenReturn((FacebookNodeList) this.serializer.deserialize("{\"data\":[{\n" +
                        "\"permission\":\"manage_pages\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"pages_show_list\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"pages_messaging\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"public_profile\",\"status\":\"granted\"}]}", FacebookNodeList.class));

        this.integLogic = new AIIntegrationLogic(this.fakeConfig, this.fakeDatabase,
                this.serializer, this.fakeConnector, this.fakeLogger);
    }

    @Test
    public void testGetIntegrations() throws Database.DatabaseException {
        List<Integration> list = Collections.singletonList(new Integration(1, "name", "desc", "icon", true));
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(list);
        ApiIntegrationList integ = (ApiIntegrationList) this.integLogic.getIntegrations(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(list, integ.getIntegrationList());
    }

    @Test
    public void testGetIntegrations_emptyList() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenReturn(new ArrayList<>());
        ApiResult result = this.integLogic.getIntegrations(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetIntegrations_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAiIntegrationList()).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.integLogic.getIntegrations(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testFacebookState_NoRecord() throws Database.DatabaseException, FacebookException {
        when(this.fakeDatabase.getIntegration(any(), any(), any())).thenReturn(null);
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(false, ((ApiFacebookIntegration) integ).hasAccessToken());
    }

    @Test
    public void testFacebookState_NoMetadata() throws Database.DatabaseException, FacebookException {
        when(this.fakeIntegrationRecord.getData()).thenReturn("");
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookState_TokenExpired() throws Database.DatabaseException, FacebookException {
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().minusHours(1));
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(false, ((ApiFacebookIntegration) integ).hasAccessToken());
    }

    @Test
    public void testFacebookState_PageList() throws Database.DatabaseException, FacebookException {
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(true, ((ApiFacebookIntegration) integ).hasAccessToken());
        Assert.assertEquals(1, ((ApiFacebookIntegration) integ).getPageList().size());
        Assert.assertTrue(((ApiFacebookIntegration) integ).getSuccess());
    }

    @Test
    public void testFacebookState_PageList_BadToken() throws Database.DatabaseException, FacebookException {
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        when(this.fakeConnector.getUserPages(anyString())).thenThrow(
                new FacebookException.FacebookAuthException(400, "fake", "OAuthException", "fake", 190));
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertFalse(((ApiFacebookIntegration) integ).getSuccess());
    }

    @Test
    public void testFacebookState_Page() throws Database.DatabaseException, FacebookException {
        when(this.fakeIntegrationRecord.getIntegrationResource()).thenReturn(TestDataHelper.ALT_SESSIONID.toString());
        ApiResult integ = this.integLogic.getFacebookState(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        Assert.assertEquals(true, ((ApiFacebookIntegration) integ).hasAccessToken());
        Assert.assertEquals(TestDataHelper.ALT_SESSIONID.toString(),
                ((ApiFacebookIntegration) integ).getPageIntegratedId());
    }

    @Test
    public void testFacebookConnect() throws Database.DatabaseException, FacebookException {
        FacebookConnect connect = new FacebookConnect();
        ApiResult integ = this.integLogic.facebookConnect(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, connect);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookConnect_ShortLived() throws Database.DatabaseException, FacebookException {
        when(this.fakeConnector.isShortLivedToken(any())).thenReturn(true);
        when(this.fakeConnector.getLongFromShortLivedToken(any())).thenReturn(this.fakeToken);
        FacebookConnect connect = new FacebookConnect();
        ApiResult integ = this.integLogic.facebookConnect(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, connect);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookConnect_Error() throws Database.DatabaseException, FacebookException {
        when(this.fakeConnector.getFacebookUserToken(any())).thenThrow(new FacebookException("fake error"));
        FacebookConnect connect = new FacebookConnect();
        ApiResult integ = this.integLogic.facebookConnect(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, connect);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookConnect_AlreadyInUse() throws Database.DatabaseException, FacebookException {
        when(this.fakeDatabase.isIntegratedUserAlreadyRegistered(any(), any(), any())).thenReturn(true);
        FacebookConnect connect = new FacebookConnect();
        ApiResult integ = this.integLogic.facebookConnect(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, connect);
        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, integ.getStatus().getCode());
        verify(this.fakeDatabase, times(1)).updateIntegration(
                any(), any(), any(), any(), not(Matchers.eq("userid")),
                Matchers.eq("{}"), any(), Matchers.eq(false));
    }

    @Test
    public void testFacebookAction_BadAction() throws Database.DatabaseException, FacebookException {
        ApiResult integ = this.integLogic.facebookAction(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, "bad", "none");
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookAction_NoValidIntegration() throws Database.DatabaseException, FacebookException {
        when(this.fakeDatabase.getIntegration(any(), any(), any())).thenReturn(null);
        ApiResult integ = this.integLogic.facebookAction(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, "bad", "none");
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, integ.getStatus().getCode());
    }

    @Test
    public void testFacebookAction_Page() throws Database.DatabaseException, FacebookException {
        ApiResult integ = this.integLogic.facebookAction(TestDataHelper.DEVID_UUID, TestDataHelper.AIID,
                "page", TestDataHelper.ALT_SESSIONID.toString());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        verify(this.fakeDatabase, times(1)).updateIntegration(any(), any(), any(),
                Matchers.eq(TestDataHelper.ALT_SESSIONID.toString()), any(), any(), any(), Matchers.eq(true));
    }

    @Test
    public void testFacebookAction_Disconnect() throws Database.DatabaseException, FacebookException {
        ApiResult integ = this.integLogic.facebookAction(TestDataHelper.DEVID_UUID, TestDataHelper.AIID,
                "disconnect", "");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, integ.getStatus().getCode());
        verify(this.fakeConnector, times(1)).pageUnsubscribe(any(), any());
        verify(this.fakeDatabase, times(1)).updateIntegration(any(), any(), any(),
                any(), any(), any(), any(), Matchers.eq(false));
    }

    @Test
    public void testIntegrationDelete_None() throws Database.DatabaseException, FacebookException {
        when(this.fakeDatabase.getIntegration(any(), any(), any())).thenReturn(null);
        this.integLogic.deleteIntegrations(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        verify(this.fakeConnector, times(0)).pageUnsubscribe(any(), any());
        verify(this.fakeDatabase, times(0)).deleteIntegration(any(), any(), any());
    }

    @Test
    public void testIntegrationDelete_NoToken() throws Database.DatabaseException, FacebookException {
        FacebookIntegrationMetadata metadata = new FacebookIntegrationMetadata(
                "access", "username", DateTime.now().plusHours(1));
        metadata.setPageToken("");
        when(this.fakeIntegrationRecord.getData()).thenReturn(this.serializer.serialize(metadata));
        this.integLogic.deleteIntegrations(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        verify(this.fakeConnector, times(0)).pageUnsubscribe(any(), any());
        verify(this.fakeDatabase, times(1)).deleteIntegration(any(), any(), any());
    }

    @Test
    public void testIntegrationDelete_UnsubscribeFailed() throws Database.DatabaseException, FacebookException {
        doThrow(new FacebookException("fake")).when(this.fakeConnector).pageUnsubscribe(any(), any());
        this.integLogic.deleteIntegrations(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        verify(this.fakeDatabase, times(1)).deleteIntegration(any(), any(), any());
    }

    @Test
    public void testIntegrationDelete_DeleteFailed() throws Database.DatabaseException, FacebookException {
        doThrow(new Database.DatabaseException("fake")).when(this.fakeDatabase).deleteIntegration(any(), any(), any());
        this.integLogic.deleteIntegrations(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        verify(this.fakeConnector, times(1)).pageUnsubscribe(any(), any());
    }

    @Test
    public void testIntegrationDelete_OK() throws Database.DatabaseException, FacebookException {
        this.integLogic.deleteIntegrations(TestDataHelper.DEVID_UUID, TestDataHelper.AIID);
        verify(this.fakeConnector, times(1)).pageUnsubscribe(any(), any());
        verify(this.fakeDatabase, times(1)).deleteIntegration(any(), any(), any());
    }

    @Test
    public void testPermissions_Match() throws FacebookException {
        TestPermissions integ = new TestPermissions(new String[]{"A", "B"});
        when(this.fakeConnector.getUserGrantedPermissions(anyString(), anyString()))
                .thenReturn((FacebookNodeList) this.serializer.deserialize("{\"data\":[{\n" +
                        "\"permission\":\"A\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"B\",\"status\":\"granted\"}]}", FacebookNodeList.class));
        integ.checkGrantedPermissions("1", "2");
    }

    @Test(expected = FacebookException.FacebookMissingPermissionsException.class)
    public void testPermissions_Missing() throws FacebookException {
        TestPermissions integ = new TestPermissions(new String[]{"X", "Y"});
        when(this.fakeConnector.getUserGrantedPermissions(anyString(), anyString()))
                .thenReturn((FacebookNodeList) this.serializer.deserialize("{\"data\":[{\n" +
                        "\"permission\":\"A\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"D\",\"status\":\"granted\"}]}", FacebookNodeList.class));
        integ.checkGrantedPermissions("1", "2");
    }

    @Test(expected = FacebookException.FacebookMissingPermissionsException.class)
    public void testPermissions_Denied() throws FacebookException {
        TestPermissions integ = new TestPermissions(new String[]{"A", "B"});
        when(this.fakeConnector.getUserGrantedPermissions(anyString(), anyString()))
                .thenReturn((FacebookNodeList) this.serializer.deserialize("{\"data\":[{\n" +
                        "\"permission\":\"A\",\"status\":\"granted\"},\n" +
                        "{\"permission\":\"B\",\"status\":\"not granted\"}]}", FacebookNodeList.class));
        integ.checkGrantedPermissions("1", "2");
    }

    class TestPermissions extends AIIntegrationLogic {

        String[] required;

        public TestPermissions(String[] required) {
            super(TestAIIntegrationLogic.this.fakeConfig, TestAIIntegrationLogic.this.fakeDatabase,
                    TestAIIntegrationLogic.this.serializer, TestAIIntegrationLogic.this.fakeConnector,
                    TestAIIntegrationLogic.this.fakeLogger);
            this.required = required;
        }

        @Override
        protected String[] getRequiredPermissions() {
            return this.required;
        }
    }

}
