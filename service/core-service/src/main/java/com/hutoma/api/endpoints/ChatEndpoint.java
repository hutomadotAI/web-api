package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.chat.WebhookHandler;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;
import com.webcohesion.enunciate.metadata.rs.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * AI Chat endpoint.
 */
@Path("/ai/")
public class ChatEndpoint {

    private final ChatLogic chatLogic;
    private final JsonSerializer serializer;
    private final WebhookHandler webhookHandler;

    @Inject
    public ChatEndpoint(final ChatLogic chatLogic,
                        final WebhookHandler webhookHandler,
                        final JsonSerializer serializer) {
        this.chatLogic = chatLogic;
        this.webhookHandler = webhookHandler;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/chat")
    @RateLimit(RateKey.Chat)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatQuestion})
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_ACCEPTED, condition = "Unable to respond in time, try again"),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "The AI is not trained"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("q"), @QueryParam("chatId")},
            output = ChatResult.class
    )
    public
    @TypeHint(ChatResult.class)
    Response chat(
            @Context ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        Map<String, String> chatheaders = HeaderUtils.getClientVariablesFromHeaders(headers);
        ApiResult result = this.chatLogic.chat(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatQuestion(requestContext),
                ParameterFilter.getChatID(requestContext),
                chatheaders);
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{aiid}/chat/target")
    @RateLimit(RateKey.Chat)
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatHandoverTarget})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Invalid chat target"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("chatId"), @QueryParam("target")}
    )
    public Response handOverChat(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.handOver(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatID(requestContext),
                ParameterFilter.getChatHandoverTarget(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("{aiid}/chat/setVariables")
    @RateLimit(RateKey.Chat)
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Invalid variables"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("chatId")}
    )
    @ValidatePost({APIParameter.ContextVariables})
    public Response setVariable(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.setContextVariable(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatID(requestContext),
                ParameterFilter.getContextVariables(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("{aiid}/chat/triggerIntent")
    @RateLimit(RateKey.Chat)
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID, APIParameter.IntentName})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Invalid variables"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {
                    @QueryParam("intentName"),
                    @QueryParam("chatId")
            }
    )
    public Response triggerIntent(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.triggerIntent(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatID(requestContext),
                ParameterFilter.getIntentName(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{aiid}/chat/reset")
    @RateLimit(RateKey.Chat)
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("chatId")}
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetChat(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.resetChat(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatID(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("load/{aiid}/chat")
    @RateLimit(RateKey.LoadTest)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatQuestion})
    @Secured({Role.ROLE_TEST})
    @Produces(MediaType.APPLICATION_JSON)
    public Response chatLoadTest(
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.chat(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatQuestion(requestContext),
                ParameterFilter.getChatID(requestContext),
                null);
        return result.getResponse(this.serializer).build();
    }


    @POST
    @Path("/chat/webhook/callback/{chat_id_hash}")
    @ValidatePost({APIParameter.WebHookReponse})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Invalid variables"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Webhook token")
    })
    @ResourceMethodSignature(
            pathParams = {
                    @PathParam("chat_id_hash")
            }
    )
    public Response runCallback(
            @Context ContainerRequestContext requestContext,
            @PathParam("chat_id_hash") String chatIdHash) {
        ApiResult result = this.webhookHandler.runWebhookCallback(chatIdHash,
                ParameterFilter.getWebHookResponse(requestContext));
        return result.getResponse(this.serializer).build();
    }
}