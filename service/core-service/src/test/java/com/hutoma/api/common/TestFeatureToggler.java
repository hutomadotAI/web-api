package com.hutoma.api.common;

import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseFeatures;
import com.hutoma.api.logging.ILogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TestFeatureToggler {

    private static final String FT_NAME = "feature1";
    private static final String STATE_T1 = "T1";

    private DatabaseFeatures fakeDatabaseFeatures;
    private ILogger fakeLogger;
    private Config fakeConfig;

    public TestFeatureToggler() {
        this.fakeDatabaseFeatures = mock(DatabaseFeatures.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeConfig = mock(Config.class);
    }

    @Test
    public void testFeatureToggler_storate_read() throws DatabaseException {
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        verify(this.fakeDatabaseFeatures).getAllFeatures();
    }

    @Test
    public void testFeatureToggler_storate_read_exception() throws DatabaseException {
        when(this.fakeDatabaseFeatures.getAllFeatures()).thenThrow(DatabaseException.class);
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        verify(this.fakeLogger).logException(any(), any());
    }

    @Test
    public void testFeatureToggler_storate_reload() throws DatabaseException, InterruptedException {
        when(this.fakeConfig.getFeatureToggleReadIntervalSec()).thenReturn(0);
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Thread.sleep(10);
        ft.getState(FT_NAME);
        verify(this.fakeDatabaseFeatures, times(2)).getAllFeatures();
    }

    @Test
    public void testFeatureToggler_getGlobal() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(null, null, FT_NAME, STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }

    @Test
    public void testFeatureToggler_getGlobal_notDefined() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(null, null, "some other", STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }

    @Test
    public void testFeatureToggler_getForDev() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(TestDataHelper.DEVID_UUID, null, FT_NAME, STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }

    @Test
    public void testFeatureToggler_getForDev_notDefined() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(TestDataHelper.DEVID_UUID, null, "some other", STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }

    @Test
    public void testFeatureToggler_getForAIID() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(null, TestDataHelper.AIID, FT_NAME, STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.T1, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }

    @Test
    public void testFeatureToggler_getForAIID_notDefined() throws DatabaseException {
        List<DatabaseFeatures.DatabaseFeature> features = new ArrayList<>();
        features.add(new DatabaseFeatures.DatabaseFeature(null, TestDataHelper.AIID, "some other", STATE_T1));
        doReturn(features).when(this.fakeDatabaseFeatures).getAllFeatures();
        FeatureToggler ft = new FeatureToggler(this.fakeDatabaseFeatures, this.fakeLogger, this.fakeConfig);
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getState(FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateForAiid(TestDataHelper.DEVID_UUID, TestDataHelper.AIID, FT_NAME));
        Assert.assertEquals(FeatureToggler.FeatureState.C, ft.getStateforDev(TestDataHelper.DEVID_UUID, FT_NAME));
    }
}
