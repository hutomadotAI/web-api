package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;

/**
 * Created by David MG on 09/08/2016.
 */
@Path("/ai/")
@RateLimit(RateKey.QuickRead)
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
    @Secured( {Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters( {APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadTrainingFile(@Context SecurityContext securityContext,
                                       @Context ContainerRequestContext requestContext,
                                       @DefaultValue("") @HeaderParam("_developer_id") String devid,
                                       @DefaultValue("0") @QueryParam("source_type") int type,
                                       @DefaultValue("") @QueryParam("url") String url,
                                       @FormDataParam("file") InputStream uploadedInputStream,
                                       @FormDataParam("file") FormDataContentDisposition fileDetail) {
        ApiResult result = this.trainingLogic.uploadFile(securityContext, devid,
            ParameterFilter.getAiid(requestContext),
            type, url, uploadedInputStream, fileDetail);
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/start")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingStart(@Context SecurityContext securityContext,
                                  @Context ContainerRequestContext requestContext,
                                  @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = trainingLogic.startTraining(securityContext, devid, ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }
    @PUT
    @Path("/{aiid}/training/stop")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingStop(@Context SecurityContext securityContext,
                                 @Context ContainerRequestContext requestContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = trainingLogic.stopTraining(securityContext, devid, ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/update")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingUpdate( @Context SecurityContext securityContext,
                                    @Context ContainerRequestContext requestContext,
                                    @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        ApiResult result = trainingLogic.updateTraining(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/delete")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingDelete( @Context SecurityContext securityContext,
                                    @Context ContainerRequestContext requestContext,
                                    @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        ApiResult result = this.trainingLogic.delete(securityContext, devid,
            ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

}