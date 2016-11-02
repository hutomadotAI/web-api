package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.ChatLogic;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 08/08/2016.
 */
@Path("/ai/")
@RateLimit(RateKey.Chat)
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
    @ValidateParameters({APIParameter.AIID, APIParameter.ChatID, APIParameter.ChatQuestion, APIParameter.ChatHistory,
            APIParameter.ChatTopic, APIParameter.Min_P})
    @Secured({Role.ROLE_CLIENTONLY, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3,
            Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response chat(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.chatLogic.chat(securityContext,
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getChatQuestion(requestContext),
                ParameterFilter.getChatID(requestContext),
                ParameterFilter.getChatHistory(requestContext),
                ParameterFilter.getTopic(requestContext),
                ParameterFilter.getMinP(requestContext));
        return result.getResponse(this.serializer).build();
    }

}