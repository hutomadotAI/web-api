package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.logic.ChatLogic;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 08/08/2016.
 */
@Path("/ai/")
public class ChatEndpoint {

    @Inject
    ChatLogic chatLogic;

    @GET
    @Path("{aiid}/chat")
    @Secured({Role.ROLE_CLIENTONLY,Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String chat( @Context SecurityContext securityContext,
                        @PathParam("aiid") String aiid,
                        @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                        @DefaultValue("") @QueryParam("q") String q,
                        @DefaultValue("1") @QueryParam("uid") String uid,
                        @DefaultValue("") @QueryParam("history") String history,
                        @DefaultValue("false") @QueryParam("active_learning") boolean on_the_fly_learning,
                        @DefaultValue("false") @QueryParam("fs") boolean fs,
                        @DefaultValue("0.5") @QueryParam("min_p") float min_p) {
        return chatLogic.chat(securityContext, aiid, dev_id, q, uid, history, on_the_fly_learning, fs, min_p);
    }

}