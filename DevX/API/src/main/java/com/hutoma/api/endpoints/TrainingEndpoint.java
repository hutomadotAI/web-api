package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.TrainingLogic;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;

/**
 * Created by David MG on 09/08/2016.
 */
@Path("/ai/")
public class TrainingEndpoint {

    TrainingLogic trainingLogic;
    JsonSerializer serializer;

    @Inject
    public TrainingEndpoint(TrainingLogic trainingLogic, JsonSerializer serializer) {
        this.trainingLogic = trainingLogic;
        this.serializer = serializer;
    }

    @POST
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadTrainingFile(@Context SecurityContext securityContext,
                                       @DefaultValue("") @HeaderParam("_developer_id") String devid,
                                       @PathParam("aiid") String aiid,
                                       @DefaultValue("0") @QueryParam("source_type") int type,
                                       @DefaultValue("")  @QueryParam("url") String url,
                                       @FormDataParam("file") InputStream uploadedInputStream,
                                       @FormDataParam("file") FormDataContentDisposition fileDetail) {
        ApiResult result = trainingLogic.uploadFile(securityContext, devid, aiid, type, url, uploadedInputStream, fileDetail);
        return result.getResponse(serializer).build();
    }

    @DELETE
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response delete( @Context SecurityContext securityContext,
                          @DefaultValue("") @HeaderParam("_developer_id") String devid,
                          @PathParam("aiid") String aiid) {
        ApiResult result = trainingLogic.delete(securityContext, devid, aiid);
        return result.getResponse(serializer).build();
    }

}