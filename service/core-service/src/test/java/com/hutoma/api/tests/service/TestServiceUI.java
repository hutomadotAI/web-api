package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUI;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;
import com.hutoma.api.endpoints.UIEndpoint;
import com.hutoma.api.logic.UILogic;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

import static com.hutoma.api.common.DeveloperInfoHelper.DEVINFO;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by pedrotei on 28/03/17.
 */
public class TestServiceUI extends ServiceTestBase {

    private static final String UI_PATH = "/ui";
    private static final String UI_PATH_BOTSTORE = UI_PATH + "/botstore";
    private static final String UI_PATH_AIDETAILS = UI_PATH + "/ai/" + AIID + "/details";

    @Mock
    private DatabaseUI fakeDatabaseUi;

    @Test
    public void testGetBotstoreItems_authenticated() throws DatabaseException {
        testCallingGetBotStoreItems(true);
    }

    @Test
    public void testGetBotstoreItems_unAuthenticated() throws DatabaseException {
        testCallingGetBotStoreItems(false);
    }

    @Test
    public void testGetBotstoreItem_authenticated() throws DatabaseException {
        testCallingGetBotStoreItem(true);
    }

    @Test
    public void testGetBotstoreItem_unAuthenticated() throws DatabaseException {
        testCallingGetBotStoreItem(false);
    }

    @Test
    public void testGetBotstoreItems_commaSeparatedFilterList() throws DatabaseException {
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new ApiBotstoreItemList(Collections.emptyList(), 0, 1, 1));
        final Response response = target(UI_PATH_BOTSTORE)
                .queryParam("filter", "a,b")
                .request()
                .headers(noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        List<String> expectedFilters = Arrays.asList("a", "b");
        verify(this.fakeDatabaseUi).getBotstoreList(Integer.valueOf(UIEndpoint.DEFAULT_START_FROM),
                Integer.valueOf(UIEndpoint.DEFAULT_PAGE_SIZE), expectedFilters, UIEndpoint.DEFAULT_ORDER_FIELD,
                UIEndpoint.DEFAULT_ORDER_DIR);
    }

    @Test
    public void testGetAiDetails() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        final Response response = target(UI_PATH_AIDETAILS)
                .request()
                .headers(defaultHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    private void testCallingGetBotStoreItem(final boolean isAuthenticated) throws DatabaseException {
        BotstoreItem item = new BotstoreItem(0, SAMPLEBOT, DEVINFO, false);
        when(this.fakeDatabaseUi.getBotstoreItem(anyInt(), any())).thenReturn(item);
        final Response response = target(UI_PATH_BOTSTORE + "/" + SAMPLEBOT.getBotId())
                .request()
                .headers(isAuthenticated ? defaultHeaders : noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
    }

    private void testCallingGetBotStoreItems(final boolean isAuthenticated) throws DatabaseException {
        BotstoreItem item = new BotstoreItem(0, SAMPLEBOT, DEVINFO, false);
        ApiBotstoreItemList itemList = new ApiBotstoreItemList(Collections.singletonList(item), 0, 1, 1);
        when(this.fakeDatabaseUi.getBotstoreList(anyInt(), anyInt(), any(), any(), any())).thenReturn(itemList);
        final Response response = target(UI_PATH_BOTSTORE)
                .request()
                .headers(isAuthenticated ? defaultHeaders : noDevIdHeaders)
                .get();
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
