package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseUI;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;
import com.hutoma.api.endpoints.UIEndpoint;
import com.hutoma.api.logic.UILogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.Collections;
import javax.ws.rs.core.Response;

import static com.hutoma.api.common.DeveloperInfoHelper.DEVINFO;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 28/03/17.
 */
public class TestServiceUI extends ServiceTestBase {

    private static final String UI_PATH = "/ui";
    private static final String UI_PATH_BOTSTORE = UI_PATH + "/botstore";

    @Mock
    private DatabaseUI fakeDatabaseUi;

    @Test
    public void testGetBotstoreItems_authenticated() throws Database.DatabaseException {
        testCallingGetBotStoreItems(true);
    }

    @Test
    public void testGetBotstoreItems_unAuthenticated() throws Database.DatabaseException {
        testCallingGetBotStoreItems(false);
    }

    @Test
    public void testGetBotstoreItem_authenticated() throws Database.DatabaseException {
        testCallingGetBotStoreItem(true);
    }

    @Test
    public void testGetBotstoreItem_unAuthenticated() throws Database.DatabaseException {
        testCallingGetBotStoreItem(false);
    }

    private void testCallingGetBotStoreItem(final boolean isAuthenticated) throws Database.DatabaseException {
        BotstoreItem item = new BotstoreItem(0, SAMPLEBOT, DEVINFO, false);
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt())).thenReturn(item);
        final Response response = target(UI_PATH_BOTSTORE + "/" + SAMPLEBOT.getBotId()).request().headers(isAuthenticated ? defaultHeaders : noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    private void testCallingGetBotStoreItems(final boolean isAuthenticated) throws Database.DatabaseException {
        BotstoreItem item = new BotstoreItem(0, SAMPLEBOT, DEVINFO, false);
        ApiBotstoreItemList itemList = new ApiBotstoreItemList(Collections.singletonList(item), 0, 1, 1);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        final Response response = target(UI_PATH_BOTSTORE).request().headers(isAuthenticated ? defaultHeaders : noDevIdHeaders).get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    protected Class<?> getClassUnderTest() {
        return UIEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {

        this.fakeDatabaseUi = mock(DatabaseUI.class);

        binder.bind(UILogic.class).to(UILogic.class);

        binder.bindFactory(new InstanceFactory<>(TestServiceUI.this.fakeDatabaseUi)).to(DatabaseUI.class);

        return binder;
    }
}