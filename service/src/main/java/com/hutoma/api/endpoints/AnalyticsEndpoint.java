package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logic.AnalyticsLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Analytics endpoints
 */
@Path("/insights/")
public class AnalyticsEndpoint {
    private final AnalyticsLogic analyticsLogic;
    private final JsonSerializer serializer;

    @Inject
    public AnalyticsEndpoint(final AnalyticsLogic analyticsLogic, final JsonSerializer serializer) {
        this.analyticsLogic = analyticsLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/chatlogs")
    @RateLimit(RateKey.Analytics)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.AnalyticsResponseFormat})
    public
    @TypeHint(ChatResult.class)
    Response getChatLogs(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @QueryParam("from") String fromDate,
            @DefaultValue("") @QueryParam("to") String toDate) {
        ApiResult result = this.analyticsLogic.getChatLogs(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                fromDate,
                toDate,
                ParameterFilter.getAnalyticsResponseFormat(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
