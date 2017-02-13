package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.logic.AIBotStoreLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.UUID;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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

    @Path("{botId}")
    @GET
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public
    @TypeHint(AiBot.class)
    Response getBotDetails(
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiBotStoreLogic.getBotDetails(botId);
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
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "Bot already purchased; Cannot purchase owned bot"),
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

    @POST
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition =
                    "Developer information hasn't been update yet; Invalid publish information; "
                            + "AI already has a published bot; "
                            + "Publishing an AI that is already linked to one or more bots is not supported"
                            + "AI needs to be fully trained before being allowed to be published"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    // TODO: need to validate the bot data
    public
    @TypeHint(AiBot.class)
    Response publishBot(
            @Context ContainerRequestContext requestContext,
            @NotNull final @FormParam("aiid") String aiid,
            @NotNull final @FormParam("name") String name,
            @NotNull final @FormParam("description") String description,
            @NotNull final @FormParam("longDescription") String longDescription,
            @NotNull final @FormParam("alertMessage") String alertMessage,
            @DefaultValue("") final @FormParam("badge") String badge,
            @NotNull final @FormParam("price") BigDecimal price,
            @NotNull final @FormParam("sample") String sample,
            @NotNull final @FormParam("category") String category,
            @NotNull final @FormParam("privacyPolicy") String privacyPolicy,
            @NotNull final @FormParam("classification") String classification,
            @NotNull final @FormParam("version") String version,
            @NotNull final @FormParam("licenseType") String licenseType,
            @DefaultValue("") final @FormParam("videoLink") String videoLink
    ) {
        ApiResult result = this.aiBotStoreLogic.publishBot(
                ParameterFilter.getDevid(requestContext),
                UUID.fromString(aiid),
                name,
                description,
                longDescription,
                alertMessage,
                badge,
                price,
                sample,
                category,
                licenseType,
                privacyPolicy,
                classification,
                version,
                videoLink);
        return result.getResponse(this.serializer).build();
    }

    @Path("{botId}/icon")
    @GET
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.TEXT_PLAIN)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response getBotIcon(
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiBotStoreLogic.getBotIcon(botId);
        return result.getResponse(this.serializer).build();
    }

    @Path("{botId}/icon")
    @POST
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Bot not found."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response uploadBotIcon(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("file") InputStream uploadedInputStream
    ) {
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(
                ParameterFilter.getDevid(requestContext),
                botId,
                uploadedInputStream,
                fileDetail);
        return result.getResponse(this.serializer).build();
    }
}