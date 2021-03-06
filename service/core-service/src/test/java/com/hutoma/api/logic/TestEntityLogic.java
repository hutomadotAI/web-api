package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.EntityValueType;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.logging.ILogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.*;

import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 06/10/2016.
 */
public class TestEntityLogic {

    private static final String ENTITY_NAME = "entity";
    private static final OptionalInt ENTITY_ID = OptionalInt.of(123);
    private static final UUID AIID = UUID.randomUUID();
    private DatabaseEntitiesIntents fakeDatabase;
    private Config fakeConfig;
    private EntityLogic entityLogic;
    private ILogger fakeLogger;
    private TrainingLogic trainingLogic;
    private EntityRecognizerService entityRecognizerService;
    private JsonSerializer jsonSerializer;
    private FeatureToggler fakeFeatureToggler;

    @Before
    public void setup() throws EntityRecognizerService.EntityRecognizerException {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.entityRecognizerService = mock(EntityRecognizerService.class);
        this.jsonSerializer = mock(JsonSerializer.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);
        this.entityLogic = new EntityLogic(this.fakeConfig,
                this.fakeLogger,
                this.fakeDatabase,
                this.trainingLogic,
                this.entityRecognizerService,
                this.jsonSerializer,
                this.fakeFeatureToggler);

        when(this.fakeConfig.getMaxTotalEntityValues()).thenReturn(1000);
        when(this.fakeConfig.getMaxEntityValuesPerEntity()).thenReturn(500);
        when(this.entityRecognizerService.findEntities(any(), any())).thenReturn("fake response");
    }

    @Test
    public void testGetEntities_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), any())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Success_Return() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), any())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID, AIID);
        Assert.assertEquals(1, ((ApiEntityList) result).getEntities().size());
        Assert.assertEquals(ENTITY_NAME, ((ApiEntityList) result).getEntities().get(0).getName());
    }

    @Test
    public void testGetEntities_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), any())).thenReturn(new ArrayList<>());
        final ApiEntityList result = (ApiEntityList) this.entityLogic.getEntities(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getEntities().isEmpty());
    }

    @Test
    public void testGetEntities_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), any())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString(), any())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success_Return() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString(), any())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(ENTITY_NAME, ((ApiEntity) result).getEntityName());
    }

    @Test
    /***
     * If the caller gets a single entity that doesn't exist, then the return is "success" with
     * an empty value list
     */
    public void testGetEntity_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString(), any())).thenReturn(getEntityEmpty());
        final ApiEntity result = (ApiEntity) this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ENTITY_NAME, result.getEntityName());
        Assert.assertTrue(result.getEntityValueList().isEmpty());
    }

    @Test
    public void testGetEntity_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString(), any())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Success() {
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID), AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Update_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString(), any())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID), AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Error() throws DatabaseException {
        doThrow(DatabaseException.class).when(this.fakeDatabase).writeEntity(any(), anyString(), any(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID), AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_RenameClash() throws DatabaseException {
        doThrow(new DatabaseIntegrityViolationException(new Exception("test"))).when(this.fakeDatabase).writeEntity(any(), anyString(), any(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity("nameclash", DEVID_UUID), AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), any(), any())).thenReturn(new ApiEntity(ENTITY_NAME, DEVID_UUID));
        when(this.fakeDatabase.deleteEntityByName(any(), any(), any(), any())).thenReturn(true);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), any(), any())).thenReturn(new ApiEntity(ENTITY_NAME, DEVID_UUID));
        when(this.fakeDatabase.deleteEntityByName(any(), any(), any(), any())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), any(), any())).thenReturn(null);
        when(this.fakeDatabase.deleteEntityByName(any(), any(), any(), any())).thenReturn(false);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_entityInUse_doesNotDeleteEntity() throws DatabaseException {
        UUID aiid = UUID.randomUUID();
        ApiIntent intent = TestIntentLogic.getIntent();
        intent.getVariables().add(new IntentVariable(ENTITY_NAME, DEVID_UUID, true, 1, "value", false, "label", false));
        when(this.fakeDatabase.getAllIntents(DEVID_UUID)).thenReturn(Collections.singletonList(intent));
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        verify(this.fakeDatabase, never()).deleteEntityByName(any(), any(), any(), any());
    }

    @Test
    public void testDeleteEntity_entityNotInUse_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAllIntents(DEVID_UUID)).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.deleteEntityByName(any(), any(), any(), any())).thenReturn(true);
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testDeleteEntity_dbError_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAllIntents(DEVID_UUID)).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.deleteEntityByName(any(), any(), any(), any())).thenThrow(DatabaseException.class);
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME, AIID);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_dbError_doesNotStopTraining() throws DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAllIntents(any())).thenThrow(DatabaseException.class);
        this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, getEntity(), any());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_entityNotInUse_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAllIntents(DEVID_UUID)).thenReturn(Collections.emptyList());
        this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, getEntity(), any());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testEntityMax_exceedsEntityMaxValues() {
        final int maxValuesPerEntity = 5;
        when(this.fakeConfig.getMaxEntityValuesPerEntity()).thenReturn(maxValuesPerEntity);
        final ApiEntity entity = getEntityWithNValues(maxValuesPerEntity + 1);
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, entity.getEntityName(), entity, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testEntityMax_exceedsEntityMaxDevValues() throws DatabaseException {
        final int maxValuesPerDev = 5;
        when(this.fakeConfig.getMaxTotalEntityValues()).thenReturn(maxValuesPerDev);
        when(this.fakeDatabase.getEntityValuesCountForDevExcludingEntity(any(), any(), any())).thenReturn(maxValuesPerDev);
        // We're already at the limit, so adding one should not be possible
        final ApiEntity entity = getEntityWithNValues(1);
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, entity.getEntityName(), entity, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private ApiEntity getEntityWithNValues(final int numValues) {
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= numValues; i++) {
            values.add("value" + i);
        }
        return new ApiEntity("entity1", DEVID_UUID, values, false, EntityValueType.LIST);
    }


    private List<Entity> getEntitiesList() {
        return Collections.singletonList(new Entity(ENTITY_NAME, false, EntityValueType.LIST));
    }

    private ApiEntity getEntity() {
        return new ApiEntity(ENTITY_NAME, DEVID_UUID, Arrays.asList("oneval", "twoval"), false, EntityValueType.LIST);
    }

    private ApiEntity getEntityEmpty() {
        return new ApiEntity(ENTITY_NAME, DEVID_UUID, Arrays.asList(new String[]{}), false, EntityValueType.LIST);
    }


}
