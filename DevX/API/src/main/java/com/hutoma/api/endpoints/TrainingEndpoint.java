package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.logic.TrainingLogic;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;

/**
 * Created by David MG on 09/08/2016.
 */
@Path("/ai/")
public class TrainingEndpoint {

    @Context
    TrainingLogic trainingLogic;

    @POST
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadTrainingFile( @Context SecurityContext securityContext,
                              @DefaultValue("") @HeaderParam("_developer_id") String devid,
                              @PathParam("aiid") String aiid,
                              @DefaultValue("0") @QueryParam("source_type") int type,
                              @DefaultValue("")  @QueryParam("url") String url,
                              @FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {
        return trainingLogic.uploadFile(securityContext, devid, aiid, type, url, uploadedInputStream, fileDetail);
    }

    @DELETE
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String delete( @Context SecurityContext securityContext,
                          @DefaultValue("") @HeaderParam("_developer_id") String devid,
                          @PathParam("aiid") String aiid) {
        return trainingLogic.delete(securityContext, devid, aiid);
    }

}