package com.hutoma.api.tests.service;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.containers.ApiExperiment;
import com.hutoma.api.endpoints.ExperimentEndpoint;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestServiceExperiment extends ServiceTestBase {

    private static final String EXPERIMENT_BASEPATH = "/experiment/";
    private static final String FEATURE_PARAM = "feature";
    private static final String EXPERIMENT_NAME = "feature1";

    protected Class<?> getClassUnderTest() {
        return ExperimentEndpoint.class;
    }

    @Test
    public void testExperiment_devId() {
        when(this.fakeFeatureToggler.getStateforDev(any(), any())).thenReturn(FeatureToggler.FeatureState.T1);
        final Response response = target(EXPERIMENT_BASEPATH)
                .queryParam(FEATURE_PARAM, EXPERIMENT_NAME)
                .request()
                .headers(defaultHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiExperiment resultExperiment = deserializeResponse(response, ApiExperiment.class);
        Assert.assertEquals(EXPERIMENT_NAME, resultExperiment.getName());
        Assert.assertEquals(FeatureToggler.FeatureState.T1, resultExperiment.getState());
    }

    @Test
    public void testExperiment_devId_invalidDevId() {
        final Response response = target(EXPERIMENT_BASEPATH)
                .queryParam(FEATURE_PARAM, EXPERIMENT_NAME)
                .request()
                .headers(noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void testExperiment_aiid() {
        when(this.fakeFeatureToggler.getStateForAiid(any(), any(), any())).thenReturn(FeatureToggler.FeatureState.T1);
        final Response response = target(EXPERIMENT_BASEPATH).path(AIID.toString())
                .queryParam(FEATURE_PARAM, EXPERIMENT_NAME)
                .request()
                .headers(defaultHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getStatus());
        ApiExperiment resultExperiment = deserializeResponse(response, ApiExperiment.class);
        Assert.assertEquals(EXPERIMENT_NAME, resultExperiment.getName());
        Assert.assertEquals(FeatureToggler.FeatureState.T1, resultExperiment.getState());
    }

    @Test
    public void testExperiment_aiid_invalidDevId() {
        final Response response = target(EXPERIMENT_BASEPATH).path(AIID.toString())
                .queryParam(FEATURE_PARAM, EXPERIMENT_NAME)
                .request()
                .headers(noDevIdHeaders)
                .get();
        Assert.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.getStatus());
    }

}
