package com.hutoma.api.tests.service;

import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.endpoints.EntityEndpoint;
import com.hutoma.api.logic.EntityLogic;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;
import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestServiceEntity extends ServiceTestBase {

    private static final String BASEPATH = "/entity/";
    private static final String ENTITY_NAME_PARAM = "entity_name";
    private static final String ENTITY_NAME = "entity";
    private IMemoryIntentHandler fakeMemoryIntentHandler;

    @Test
    public void testCreateUpdateEntity_authorized() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        final Response response = target(BASEPATH)
                .queryParam(ENTITY_NAME_PARAM, ENTITY_NAME)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(getEntityJson()));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatus());
    }

    @Test
    public void testCreateUpdateEntity_notAuthorized() {
        final Response response = target(BASEPATH)
                .queryParam(ENTITY_NAME_PARAM, ENTITY_NAME)
                .request()
                .headers(noDevIdHeaders)
                .post(Entity.json(getEntityJson()));
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testCreateUpdateEntity_valuesAcceptUTF8() {
        final Response response = target(BASEPATH)
                .queryParam(ENTITY_NAME_PARAM, ENTITY_NAME)
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(getEntityJson(new String[]{"보라", "Λορεμ", "լոռեմ", "Лорем", "غينيا "})));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatus());
    }

    @Test
    public void testCreateUpdateEntity_doesNotAcceptSystemEntities_nameStartsWithSys() {
        ApiEntity entity = new ApiEntity("sys.systementity", DEVID,
                Collections.singletonList("value1"), false);
        testCreateUpdateEntity_sysEntity(entity);
    }

    @Test
    public void testCreateUpdateEntity_doesNotAcceptSystemEntities_nameDoesNitStartsWithSys() {
        ApiEntity entity = new ApiEntity("systementity", DEVID,
                Collections.singletonList("value1"), true);
        testCreateUpdateEntity_sysEntity(entity);
    }

    private void testCreateUpdateEntity_sysEntity(final ApiEntity entity) {
        final Response response = target(BASEPATH)
                .queryParam(ENTITY_NAME_PARAM, entity.getEntityName())
                .request()
                .headers(defaultHeaders)
                .post(Entity.json(serializeObject(entity)));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Override
    protected Class<?> getClassUnderTest() {
        return EntityEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        this.fakeMemoryIntentHandler = mock(IMemoryIntentHandler.class);

        binder.bind(EntityLogic.class).to(EntityLogic.class);
        binder.bind(TrainingLogic.class).to(TrainingLogic.class);

        binder.bindFactory(new InstanceFactory<>(this.fakeMemoryIntentHandler)).to(IMemoryIntentHandler.class);
        return binder;
    }

    private String getEntityJson() {
        return getEntityJson(null);
    }

    private String getEntityJson(final String[] values) {
        ApiEntity entity = new ApiEntity(ENTITY_NAME, DEVID,
                values == null ? Collections.emptyList() : new ArrayList<String>(Arrays.asList(values)), false);
        return serializeObject(entity);
    }
}
