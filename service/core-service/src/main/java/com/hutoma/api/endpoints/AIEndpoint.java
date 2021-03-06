package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;
import com.webcohesion.enunciate.metadata.rs.*;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * AI endpoint for managing a user's bots
 */
@Path("/ai/")
@RateLimit(RateKey.QuickRead)
public class AIEndpoint {

    private final AILogic aiLogic;
    private final JsonSerializer serializer;

    @Inject
    public AIEndpoint(AILogic aiLogic, JsonSerializer serializer) {
        this.aiLogic = aiLogic;
        this.serializer = serializer;
    }

    /**
     * Create a new bot
     *
     * @param requestContext
     * @param isPrivate
     * @param personality
     * @param voice
     * @return
     */
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID})
    @ValidatePost({APIParameter.AIName, APIParameter.AIDescription, APIParameter.AiConfidence,
            APIParameter.Timezone, APIParameter.Locale, APIParameter.DefaultChatResponses})
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(ApiAi.class)
    public Response createAI(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("false") @FormParam("is_private") boolean isPrivate,
            @DefaultValue("0") @FormParam("personality") int personality,
            @DefaultValue("0") @FormParam("voice") int voice,
            @DefaultValue("-1") @FormParam("error_threshold_handover") int errorThresholdHandover,
            @DefaultValue("-1") @FormParam("handover_reset_timeout_seconds") int handoverResetTimeout,
            @DefaultValue("") @FormParam("handover_message") String handoverMessage) {
        ApiResult result = this.aiLogic.createAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiName(requestContext),
                ParameterFilter.getAiDescription(requestContext),
                isPrivate,
                personality,
                ParameterFilter.getAiConfidence(requestContext),
                voice,
                ParameterFilter.getDefaultChatResponses(requestContext),
                ParameterFilter.getLocale(requestContext),
                ParameterFilter.getTimezone(requestContext),
                errorThresholdHandover,
                handoverResetTimeout,
                handoverMessage);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Update existing bot
     *
     * @param requestContext
     * @param isPrivate
     * @param personality
     * @param voice
     * @return
     */
    @Path("{aiid}")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.AIDescription, APIParameter.AiConfidence, APIParameter.Timezone,
            APIParameter.Locale, APIParameter.DefaultChatResponses})
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAI(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("false") @FormParam("is_private") boolean isPrivate,
            @DefaultValue("0") @FormParam("personality") int personality,
            @DefaultValue("0") @FormParam("voice") int voice,
            @DefaultValue("-1") @FormParam("error_threshold_handover") int errorThresholdHandover,
            @DefaultValue("-1") @FormParam("handover_reset_timeout_seconds") int handoverResetTimeout,
            @DefaultValue("") @FormParam("handover_message") String handoverMessage) {
        ApiResult result = this.aiLogic.updateAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getAiDescription(requestContext),
                isPrivate,
                personality,
                ParameterFilter.getAiConfidence(requestContext),
                voice,
                ParameterFilter.getLocale(requestContext),
                ParameterFilter.getTimezone(requestContext),
                ParameterFilter.getDefaultChatResponses(requestContext),
                errorThresholdHandover,
                handoverResetTimeout,
                handoverMessage);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Set the AI config
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}/config")
    @PUT
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost()
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Ai not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAiConfig(
            @Context ContainerRequestContext requestContext,
            AiBotConfigWithDefinition aiBotConfigWithDefinition) {
        // use botId=0 as this is the configuration of the main AI
        ApiResult result = this.aiLogic.setAiBotConfigDescription(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                aiBotConfigWithDefinition);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Regenerate a webhook secret for a bot
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}/regenerate_webhook_secret")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost()
    @Produces(MediaType.APPLICATION_JSON)
    public Response regenerateWebhookSecret(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.regenerateWebhookSecret(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get all the bots for a given user
     *
     * @param requestContext
     * @return
     */
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID})
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(ApiAiList.class)
    public Response getAIs(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.getAIs(
                ParameterFilter.getDevid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get the detail for a single bot the user owns
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(ApiAiWithConfig.class)
    public Response getSingleAI(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.getSingleAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Delete a bot
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}")
    @DELETE
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAI(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.deleteAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}/export")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportAI(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.exportBotData(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @Path("import")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID})
    @ValidatePost({APIParameter.BotStructure})
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importAI(
            @Context ContainerRequestContext requestContext, BotStructure botStructure) {
        ApiResult result = this.aiLogic.importBot(
                ParameterFilter.getDevid(requestContext), botStructure);
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}/import")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.BotStructure})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importAIInPlace(
            @Context ContainerRequestContext requestContext, BotStructure botStructure) {
        ApiResult result = this.aiLogic.importBotInPlace(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                botStructure);
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}/clone")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.AIName, APIParameter.AIDescription, APIParameter.AiConfidence,
            APIParameter.Timezone, APIParameter.Locale})
    @Produces(MediaType.APPLICATION_JSON)
    public Response cloneAI(
            @Context ContainerRequestContext requestContext,
            @DefaultValue("false") @FormParam("is_private") boolean isPrivate,
            @DefaultValue("0") @FormParam("personality") int personality,
            @DefaultValue("0") @FormParam("voice") int voice,
            @DefaultValue("") @FormParam("default_responses") String defaultResponses,
            @DefaultValue("") @FormParam("passthrough_url") String passthroughUrl) {
        ApiResult result = this.aiLogic.cloneBot(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getAiName(requestContext),
                ParameterFilter.getAiDescription(requestContext),
                isPrivate,
                personality,
                ParameterFilter.getAiConfidence(requestContext),
                voice,
                ParameterFilter.getLocale(requestContext),
                ParameterFilter.getTimezone(requestContext),
                Tools.isEmpty(defaultResponses)
                        ? new ArrayList<>()
                        : this.serializer.deserializeListAutoDetect(defaultResponses),
                passthroughUrl);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get the skills that a given bot is linked to
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}/bots")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    @TypeHint(ApiAiBotList.class)
    public Response getLinkedBots(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiLogic.getLinkedBots(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext)
        );
        return result.getResponse(this.serializer).build();
    }

    /**
     * Updates the list of the bots the AI is linked to
     *
     * @param requestContext the request context
     * @return the response
     */
    @Path("{aiid}/bots")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.BotIdList})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = ""),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response updateLinkedBots(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiLogic.updateLinkedBots(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getBotIdList(requestContext)
        );
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get the data for the linked bot, including configuration
     *
     * @param requestContext
     * @param botId
     * @return
     */
    @RateLimit(RateKey.Botstore_Metadata)
    @Path("{aiid}/bot/{botId}")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI or Bot not found."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    @TypeHint(ApiLinkedBotData.class)
    public Response getLinkedBotData(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiLogic.getLinkedBotData(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                botId);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Set the AI config for a linked bot.
     *
     * @param requestContext
     * @return
     */
    @Path("{aiid}/bot/{botId}/config")
    @PUT
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost()
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Ai not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Bot not linked"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAiConfigForBot(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId,
            AiBotConfig aiBotConfig) {
        ApiResult result = this.aiLogic.setAiBotConfig(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                botId,
                aiBotConfig);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Link this AIID to a bot in the store
     *
     * @param requestContext
     * @param botId
     * @return
     */
    @RateLimit(RateKey.Botstore_Metadata)
    @Path("{aiid}/bot/{botId}")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI or Bot not found."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "Bot not found; Bot not owned; Bot already linked"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response linkBotToAI(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiLogic.linkBotToAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                botId
        );
        return result.getResponse(this.serializer).build();
    }

    /**
     * Unlink this AIID from a bot in the store
     *
     * @param requestContext
     * @param botId
     * @return
     */
    @RateLimit(RateKey.Botstore_Metadata)
    @Path("{aiid}/bot/{botId}")
    @DELETE
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND,
                    condition = "AI or Bot not found, or not currently linked."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    public Response unlinkBotFromAI(
            @Context ContainerRequestContext requestContext,
            @PathParam("botId") int botId
    ) {
        ApiResult result = this.aiLogic.unlinkBotFromAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                botId
        );
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get the botstore view of this bot, if it is published
     *
     * @param requestContext
     * @return
     */
    @RateLimit(RateKey.Botstore_Metadata)
    @Path("{aiid}/bot")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "No bot for AI."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token.")
    })
    @TypeHint(AiBot.class)
    public Response getPublishedBotForAI(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiLogic.getPublishedBotForAI(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext)
        );
        return result.getResponse(this.serializer).build();
    }
}
