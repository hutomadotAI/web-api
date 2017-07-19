package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logic.ChatLogic;
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
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * AI Chat endpoint.
 */
@Path("/ai/")
public class ChatEndpoint {

    private final ChatLogic chatLogic;
    private final JsonSerializer serializer;

    @Inject
    public ChatEndpoint(ChatLogic chatLogic, JsonSerializer serializer) {
        this.chatLogic = chatLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/chat")
    @RateLimit(RateKey.Chat)
    @ValidateParameters({APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatQuestion})
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
        Map<String, String> chatheaders = HeaderUtils.getChatVariablesFromHeaders(headers);
        ApiResult result = this.chatLogic.chat(
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatQuestion(requestContext),
                ParameterFilter.getChatID(requestContext),
                chatheaders);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("load/{aiid}/chat")
    @RateLimit(RateKey.LoadTest)
    @ValidateParameters({APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatQuestion})
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
}