package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.MeshVariable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by David MG on 06/10/2016.
 */
public class TestMeshLogic {

    private final String DEVID = "00000000-4733-42d8-bfcf-95192131137e";
    private final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private final UUID AIID_MESH = UUID.fromString("11234567-4733-42d8-bfcf-95192131137e");
    private final String UID = "uid";
    private final String NAME = "name";
    private final String DESCRIPTION = "description";
    private final String LICENCETYPE = "licencetype";
    private final float LICENCEFEE = 0;
    private final float RATING = 0;
    private final int NACTIVATIONS = 0;
    private final String ICONPATH = "iconPath";
    private final String WCOLOR = "widgetColor";
    SecurityContext fakeContext;
    DatabaseEntitiesIntents fakeDatabase;
    Config fakeConfig;
    Tools fakeTools;
    MeshLogic meshLogic;
    Logger fakeLogger;
    private boolean ISBANNED = false;

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseEntitiesIntents.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeLogger = mock(Logger.class);
        this.meshLogic = new MeshLogic(this.fakeConfig, this.fakeLogger, this.fakeDatabase);
    }

    @Test
    public void testGetMesh_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.getMesh(anyString(), anyString())).thenReturn(getMeshList());
        final ApiResult result = this.meshLogic.getMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetMesh_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getMesh(anyString(), anyString())).thenReturn(new ArrayList<MeshVariable>());
        final ApiResult result = this.meshLogic.getMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetMesh_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getMesh(anyString(), anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.meshLogic.getMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testAddMesh_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.addMesh(any(), any(), any())).thenReturn(true);
        final ApiResult result = this.meshLogic.addMesh(this.fakeContext, this.DEVID, this.AIID, this.AIID_MESH);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testAddMesh_Error() throws Database.DatabaseException {
        doThrow(new Database.DatabaseException(new Exception("test"))).when(this.fakeDatabase).addMesh(anyString(), anyString(), any());
        final ApiResult result = this.meshLogic.addMesh(this.fakeContext, this.DEVID, this.AIID, this.AIID_MESH);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDeleteMesh_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteSingleMesh(anyString(), any(), any())).thenReturn(true);
        final ApiResult result = this.meshLogic.deleteSingleMesh(this.fakeContext, this.DEVID, this.AIID, this.AIID_MESH);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDeleteMesh_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteSingleMesh(anyString(), any(), any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.meshLogic.deleteSingleMesh(this.fakeContext, this.DEVID, this.AIID, this.AIID_MESH);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDeleteMesh_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteSingleMesh(anyString(), any(), any())).thenReturn(false);
        final ApiResult result = this.meshLogic.deleteSingleMesh(this.fakeContext, this.DEVID, this.AIID, this.AIID_MESH);
        Assert.assertEquals(404, result.getStatus().getCode());
    }


    @Test
    public void testDeleteAllMesh_Success() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAllMesh(anyString(), any())).thenReturn(true);
        final ApiResult result = this.meshLogic.deleteAllMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDeleteAllMesh_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAllMesh(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        final ApiResult result = this.meshLogic.deleteAllMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDeleteAllMesh_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAllMesh(anyString(), any())).thenReturn(false);
        final ApiResult result = this.meshLogic.deleteAllMesh(this.fakeContext, this.DEVID, this.AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }


    private List<MeshVariable> getMeshList() {
        return Arrays.asList(getMeshVariable());
    }

    private MeshVariable getMeshVariable() {
        return new MeshVariable(this.AIID.toString(), this.AIID_MESH.toString(), this.NAME, this.DESCRIPTION,
                this.LICENCETYPE, this.LICENCEFEE, this.RATING, this.NACTIVATIONS, this.ISBANNED,
                this.ICONPATH, this.WCOLOR);
    }

}
