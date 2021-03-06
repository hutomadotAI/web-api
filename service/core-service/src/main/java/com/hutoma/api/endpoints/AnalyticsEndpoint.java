package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiListMap;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;
import com.hutoma.api.logic.AnalyticsLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResourceMethodSignature;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Analytics endpoints.
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

    /**
     * Downloads a file with the chat logs within a certain timeframe.
     * @param requestContext the request context
     * @param fromDate the start date for the logs (if not provided, will be set to 30 days in the past)
     * @param toDate the end date (inclusive) for the logs (if not provided will be today)
     * @return {@link com.hutoma.api.containers.ApiString ApiString}
     */
    @GET
    @Path("{aiid}/chatlogs")
    @RateLimit(RateKey.Analytics)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.AnalyticsResponseFormat})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Dates interval incorrect"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("from"), @QueryParam("to")},
            output = ApiString.class
    )
    public
    @TypeHint(ApiString.class)
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

    /**
     * Downloads map of chat sessions within a certain timeframe.
     * @param requestContext the request context
     * @param fromDate the start date for the logs (if not provided, will be set to 30 days in the past)
     * @param toDate the end date (inclusive) for the logs (if not provided will be today)
     * @return {@link com.hutoma.api.containers.ApiListMap ApiListMap}
     */
    @GET
    @Path("{aiid}/graph/sessions")
    @RateLimit(RateKey.Analytics)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Dates interval incorrect"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("from"), @QueryParam("to")},
            output = ApiString.class
    )
    public
    @TypeHint(ApiListMap.class)
    Response getSessions(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @QueryParam("from") String fromDate,
            @DefaultValue("") @QueryParam("to") String toDate) {
        ApiResult result = this.analyticsLogic.getSessions(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                fromDate,
                toDate);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Downloads map of chat interactions within a certain timeframe.
     * @param requestContext the request context
     * @param fromDate the start date for the logs (if not provided, will be set to 30 days in the past)
     * @param toDate the end date (inclusive) for the logs (if not provided will be today)
     * @return {@link com.hutoma.api.containers.ApiListMap ApiListMap}
     */
    @GET
    @Path("{aiid}/graph/interactions")
    @RateLimit(RateKey.Analytics)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Dates interval incorrect"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("from"), @QueryParam("to")},
            output = ApiString.class
    )
    public
    @TypeHint(ApiListMap.class)
    Response getInteractions(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @QueryParam("from") String fromDate,
            @DefaultValue("") @QueryParam("to") String toDate) {
        ApiResult result = this.analyticsLogic.getInteractions(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                fromDate,
                toDate);
        return result.getResponse(this.serializer).build();
    }
}
