package com.hutoma.api.endpoints;

import com.hutoma.api.access.AuthFilter;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ui.ApiBotstoreItem;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.logic.UILogic;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResourceMethodSignature;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @Inject
    public UIEndpoint(final UILogic uiLogic, final JsonSerializer serializer, final Config config) {
        this.uiLogic = uiLogic;
        this.serializer = serializer;
        this.config = config;
    }

    private static List<String> getListFromMultipeValuedParam(final List<String> values) {
        // JAX-RS doesnt's support params with multiple values comma separated
        List<String> list = new ArrayList<>();
        if (values != null && !values.isEmpty()) {
            values.forEach(x -> {
                if (!x.isEmpty()) {
                    list.addAll(Arrays.asList(x.split(",")));
                }
            });
        }
        return list;
    }

    @GET
    @Path("botstore")
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("startFrom"), @QueryParam("pageSize"), @QueryParam("filter"),
                    @QueryParam("orderField"), @QueryParam("orderDir")},
            output = ApiBotstoreItemList.class
    )
    public Response getBotstoreList(
            @Context ContainerRequestContext requestContext,
            @DefaultValue(DEFAULT_START_FROM) @QueryParam("startFrom") int startFrom,
            @DefaultValue(DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
            @DefaultValue("") @QueryParam("filter") List<String> filters,
            @DefaultValue(DEFAULT_ORDER_FIELD) @QueryParam("orderField") String orderField,
            @DefaultValue(DEFAULT_ORDER_DIR) @QueryParam("orderDir") String orderDirection
    ) {

        ApiResult result = this.uiLogic.getBotstoreList(
                AuthFilter.getDevIdFromHeader(requestContext, this.config),
                startFrom,
                pageSize,
                getListFromMultipeValuedParam(filters),
                orderField,
                orderDirection);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("botstore/{botId}")
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            pathParams = {@PathParam("botId")},
            output = ApiBotstoreItem.class
    )
    public Response getBotstoreBot(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId) {
        ApiResult result = this.uiLogic.getBotstoreBot(
                AuthFilter.getDevIdFromHeader(requestContext, this.config),
                botId
        );
        return result.getResponse(this.serializer).build();
    }
}
