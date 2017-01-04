package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIBotStoreLogic;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBotInStore() {
        ApiResult result = this.aiBotStoreLogic.getPublishedBots();
        return result.getResponse(this.serializer).build();
    }
}