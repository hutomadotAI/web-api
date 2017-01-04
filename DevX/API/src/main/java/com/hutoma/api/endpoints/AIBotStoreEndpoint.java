package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIBotStoreLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Bot Store endpoint.
 */
@Path("/botstore")
@RateLimit(RateKey.QuickRead)
public class AIBotStoreEndpoint {

    private final AIBotStoreLogic aiBotStoreLogic;
    private final JsonSerializer serializer;

    @Inject
    public AIBotStoreEndpoint(AIBotStoreLogic aiBotStoreLogic, JsonSerializer serializer) {
        this.aiBotStoreLogic = aiBotStoreLogic;
        this.serializer = serializer;
    }

    @GET
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response getPublishedBots() {
        ApiResult result = this.aiBotStoreLogic.getPublishedBots();
        return result.getResponse(this.serializer).build();
    }

    @Path("purchased")
    @GET
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response getPurchasedBots(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiBotStoreLogic.getPurchasedBots(
                ParameterFilter.getDevid(requestContext)
        );
        return result.getResponse(this.serializer).build();
    }

    @Path("purchase/{botId}")
    @POST
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Bot already purchased."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response purchaseBot(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiBotStoreLogic.purchaseBot(
                ParameterFilter.getDevid(requestContext),
                botId
        );
        return result.getResponse(this.serializer).build();
    }
}