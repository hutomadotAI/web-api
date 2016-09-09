package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.ChatLogic;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 08/08/2016.
 */
@Path("/ai/")
public class ChatEndpoint {

    ChatLogic chatLogic;
    JsonSerializer serializer;

    @Inject
    public ChatEndpoint(ChatLogic chatLogic, JsonSerializer serializer) {
        this.chatLogic = chatLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/chat")
    @Secured({Role.ROLE_CLIENTONLY,Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response chat(
            @Context SecurityContext securityContext,
            @PathParam("aiid") String aiid,
            @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
            @DefaultValue("") @QueryParam("q") String q,
            @DefaultValue("1") @QueryParam("uid") String uid,
            @DefaultValue("") @QueryParam("chat_history") String history,
            @DefaultValue("") @QueryParam("current_topic") String topic,
            @DefaultValue("0.5") @QueryParam("confidence_threshold") float min_p) {
        ApiResult result = chatLogic.chat(securityContext, aiid, dev_id, q, uid, history, topic, min_p);
        return result.getResponse(serializer).build();
    }

}