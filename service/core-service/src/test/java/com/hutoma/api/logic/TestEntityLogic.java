package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiEntityList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.logging.ILogger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 06/10/2016.
 */
public class TestEntityLogic {

    private static final String ENTITY_NAME = "entity";
    private static final OptionalInt ENTITY_ID = OptionalInt.of(123);
    private DatabaseEntitiesIntents fakeDatabase;
    private Config fakeConfig;
    private EntityLogic entityLogic;
    private ILogger fakeLogger;
    private TrainingLogic trainingLogic;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.trainingLogic = mock(TrainingLogic.class);
        this.entityLogic = new EntityLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase, this.trainingLogic);
    }

    @Test
    public void testGetEntities_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), anyBoolean())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetEntities_Success_Return() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), anyBoolean())).thenReturn(getEntitiesList());
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID);
        Assert.assertEquals(1, ((ApiEntityList) result).getEntities().size());
        Assert.assertEquals(ENTITY_NAME, ((ApiEntityList) result).getEntities().get(0).getName());
    }

    @Test
    public void testGetEntities_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), anyBoolean())).thenReturn(new ArrayList<>());
        final ApiEntityList result = (ApiEntityList) this.entityLogic.getEntities(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getEntities().isEmpty());
    }

    @Test
    public void testGetEntities_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntities(any(), anyBoolean())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntities(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetEntity_Success_Return() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(ENTITY_NAME, ((ApiEntity) result).getEntityName());
    }

    @Test
    /***
     * If the caller gets a single entity that doesn't exist, then the return is "success" with
     * an empty value list
     */
    public void testGetEntity_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntityEmpty());
        final ApiEntity result = (ApiEntity) this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(ENTITY_NAME, result.getEntityName());
        Assert.assertTrue(result.getEntityValueList().isEmpty());
    }

    @Test
    public void testGetEntity_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.getEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Success() throws DatabaseException {
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Update_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_Error() throws DatabaseException {
        doThrow(DatabaseException.class).when(this.fakeDatabase).writeEntity(any(), anyString(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testWriteEntity_RenameClash() throws DatabaseException {
        doThrow(new DatabaseIntegrityViolationException(new Exception("test"))).when(this.fakeDatabase).writeEntity(any(), anyString(), any());
        final ApiResult result = this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity("nameclash", DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(ENTITY_ID);
        when(this.fakeDatabase.deleteEntity(any(), anyInt())).thenReturn(true);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(ENTITY_ID);
        when(this.fakeDatabase.deleteEntity(any(), anyInt())).thenThrow(DatabaseException.class);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_NotFound() throws DatabaseException {
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(OptionalInt.empty());
        when(this.fakeDatabase.deleteEntity(any(), anyInt())).thenReturn(false);
        final ApiResult result = this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDeleteEntity_entityInUse_doesNotDeleteEntity() throws DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAisForEntity(DEVID_UUID, ENTITY_NAME)).thenReturn(Collections.singletonList(aiid));
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(ENTITY_ID);
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        verify(this.fakeDatabase, never()).deleteEntity(any(), anyInt());
    }

    @Test
    public void testDeleteEntity_entityNotInUse_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID_UUID, ENTITY_NAME)).thenReturn(new ArrayList<>());
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(ENTITY_ID);
        when(this.fakeDatabase.deleteEntity(any(), anyInt())).thenReturn(true);
        when(this.fakeDatabase.getAisForEntity(any(), any())).thenReturn(Collections.emptyList());
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testDeleteEntity_dbError_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID_UUID, ENTITY_NAME)).thenReturn(Collections.singletonList(UUID.randomUUID()));
        when(this.fakeDatabase.getEntityIdForDev(any(), anyString())).thenReturn(ENTITY_ID);
        when(this.fakeDatabase.deleteEntity(any(), anyInt())).thenThrow(DatabaseException.class);
        this.entityLogic.deleteEntity(DEVID_UUID, ENTITY_NAME);
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_dbError_doesNotStopTraining() throws DatabaseException {
        UUID aiid = UUID.randomUUID();
        when(this.fakeDatabase.getAisForEntity(any(), any())).thenThrow(DatabaseException.class);
        this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, getEntity());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testWriteEntity_entityNotInUse_doesNotStopTraining() throws DatabaseException {
        when(this.fakeDatabase.getAisForEntity(DEVID_UUID, ENTITY_NAME)).thenReturn(new ArrayList<>());
        this.entityLogic.writeEntity(DEVID_UUID, ENTITY_NAME, getEntity());
        verify(this.trainingLogic, never()).stopTraining(any(), any());
    }

    @Test
    public void testReplaceEntity_entityDoesNotExist_Failure() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(null);
        final ApiResult result = this.entityLogic.replaceEntity(DEVID_UUID, "DIFFERENT_NAME", new ApiEntity("DIFFERENT_NAME", DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testReplaceEntity_Update_Success() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        final ApiResult result = this.entityLogic.replaceEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testReplaceEntity_Error() throws DatabaseException {
        when(this.fakeDatabase.getEntity(any(), anyString())).thenReturn(getEntity());
        doThrow(DatabaseException.class).when(this.fakeDatabase).writeEntity(any(), anyString(), any());
        final ApiResult result = this.entityLogic.replaceEntity(DEVID_UUID, ENTITY_NAME, new ApiEntity(ENTITY_NAME, DEVID_UUID));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }


    private List<Entity> getEntitiesList() {
        return Collections.singletonList(new Entity(ENTITY_NAME, false));
    }

    private ApiEntity getEntity() {
        return new ApiEntity(ENTITY_NAME, DEVID_UUID, Arrays.asList("oneval", "twoval"), false);
    }

    private ApiEntity getEntityEmpty() {
        return new ApiEntity(ENTITY_NAME, DEVID_UUID, Arrays.asList(new String[]{}), false);
    }


}
