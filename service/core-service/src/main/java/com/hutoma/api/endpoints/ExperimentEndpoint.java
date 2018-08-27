package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.containers.ApiExperiment;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/experiment/")
public class ExperimentEndpoint {

    private final FeatureToggler featureToggler;

    @Inject
    public ExperimentEndpoint(final FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.ExperimentFeatureName})
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResult getExperimentStatus(
            @Context final ContainerRequestContext requestContext) {
        String experimentName = ParameterFilter.getExperimentFeatureName(requestContext);
        FeatureToggler.FeatureState state = featureToggler.getStateforDev(
                ParameterFilter.getDevid(requestContext),
                experimentName);
        return new ApiExperiment(experimentName, state.toString()).setSuccessStatus();
    }

    @GET
    @Path("{aiid}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ExperimentFeatureName})
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResult getExperimentStatusForAiid(
            @Context final ContainerRequestContext requestContext) {
        String experimentName = ParameterFilter.getExperimentFeatureName(requestContext);
        FeatureToggler.FeatureState state = featureToggler.getStateForAiid(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                experimentName);
        return new ApiExperiment(experimentName, state.toString()).setSuccessStatus();
    }
}
