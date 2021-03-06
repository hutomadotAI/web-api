package com.hutoma.api.endpoints;

import com.hutoma.api.access.*;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiAiWithConfig;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ui.ApiBotstoreCategoryItemList;
import com.hutoma.api.containers.ui.ApiBotstoreItem;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.UILogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Endpoint for UI only features.
 * This endpoint should not be accessible to developers, only for internal use and to support
 * corp customers implementing their own frontend.
 */
@Path("/ui/")
public class UIEndpoint {
    public static final String DEFAULT_START_FROM = "0";
    public static final String DEFAULT_PAGE_SIZE = "100";
    public static final String DEFAULT_ORDER_FIELD = "";
    public static final String DEFAULT_ORDER_DIR = "DESC";
    private final JsonSerializer serializer;
    private final UILogic uiLogic;
    private final Config config;
    private final Provider<AILogic> aiLogicProvider;

    @Inject
    public UIEndpoint(final UILogic uiLogic,
                      final Provider<AILogic> aiLogicProvider,
                      final JsonSerializer serializer, final Config config) {
        this.uiLogic = uiLogic;
        this.aiLogicProvider = aiLogicProvider;
        this.serializer = serializer;
        this.config = config;
    }

    @GET
    @Path("botstore")
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({@ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")})
    @RequestHeaders({@RequestHeader(name = "Authorization", description = "Developer token")})
    @ResourceMethodSignature(queryParams = {@QueryParam("startFrom"), @QueryParam("pageSize"),
            @QueryParam("filter"), @QueryParam("orderField"),
            @QueryParam("orderDir")}, output = ApiBotstoreItemList.class)
    public Response getBotstoreList(@Context ContainerRequestContext requestContext,
                                    @DefaultValue(DEFAULT_START_FROM) @QueryParam("startFrom") int startFrom,
                                    @DefaultValue(DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
                                    @DefaultValue("") @QueryParam("filter") List<String> filters,
                                    @DefaultValue(DEFAULT_ORDER_FIELD) @QueryParam("orderField") String orderField,
                                    @DefaultValue(DEFAULT_ORDER_DIR) @QueryParam("orderDir") String orderDirection) {

        ApiResult result = this.uiLogic.getBotstoreList(
                AuthFilter.getDevIdFromHeader(requestContext, this.config), startFrom, pageSize,
                Tools.getListFromMultipeValuedParam(filters), orderField, orderDirection);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("botstore/per_category")
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({@ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")})
    @RequestHeaders({@RequestHeader(name = "Authorization", description = "Developer token")})
    @ResourceMethodSignature(queryParams = {@QueryParam("max"),
            @QueryParam("orderField")}, output = ApiBotstoreCategoryItemList.class)
    public Response getBotstoreListPerCategory(@Context ContainerRequestContext requestContext,
                                               @DefaultValue(DEFAULT_PAGE_SIZE) @QueryParam("max") int max) {
        ApiResult result = this.uiLogic.getBotstoreListPerCategory(
                AuthFilter.getDevIdFromHeader(requestContext, this.config), max);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("botstore/{botId}")
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({@ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")})
    @RequestHeaders({@RequestHeader(name = "Authorization", description = "Developer token")})
    @ResourceMethodSignature(pathParams = {@PathParam("botId")}, output = ApiBotstoreItem.class)
    public Response getBotstoreBot(@Context ContainerRequestContext requestContext, @PathParam("botId") int botId) {
        ApiResult result = this.uiLogic
                .getBotstoreBot(AuthFilter.getDevIdFromHeader(requestContext, this.config), botId);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("ai/{aiid}/details")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({@ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")})
    @RequestHeaders({@RequestHeader(name = "Authorization", description = "Developer token")})
    @ResourceMethodSignature(pathParams = {@PathParam("botId")}, output = ApiBotstoreItem.class)
    public Response getAiDetails(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.uiLogic.getAiDetails(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get the detail for a single bot the user owns
     * but using a different ratelimit key
     *
     * @param requestContext
     * @return
     */
    @Path("ai/{aiid}")
    @GET
    @RateLimit(RateKey.PollStatus)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(ApiAiWithConfig.class)
    public Response getSingleAIPoll(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogicProvider.get().getSingleAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
