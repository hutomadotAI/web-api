package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiTrainingMaterials;
import com.hutoma.api.logic.TrainingLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 09/08/2016.
 */
@Path("/ai/")
@RateLimit(RateKey.QuickRead)
public class TrainingEndpoint {

    private final TrainingLogic trainingLogic;
    private final JsonSerializer serializer;

    @Inject
    public TrainingEndpoint(TrainingLogic trainingLogic, JsonSerializer serializer) {
        this.trainingLogic = trainingLogic;
        this.serializer = serializer;
    }

    @POST
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
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
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingStart(@Context SecurityContext securityContext,
                                  @Context ContainerRequestContext requestContext,
                                  @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = this.trainingLogic.startTraining(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/stop")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingStop(@Context SecurityContext securityContext,
                                 @Context ContainerRequestContext requestContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = this.trainingLogic.stopTraining(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/update")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingUpdate(@Context SecurityContext securityContext,
                                   @Context ContainerRequestContext requestContext,
                                   @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        ApiResult result = this.trainingLogic.updateTraining(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/delete")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainingDelete(@Context SecurityContext securityContext,
                                   @Context ContainerRequestContext requestContext,
                                   @DefaultValue("") @HeaderParam("_developer_id") String devid) {

        ApiResult result = this.trainingLogic.delete(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("/{aiid}/training/materials")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.TEXT_PLAIN) // TODO: Produce MediaType.APPLICATION_OCTET_STREAM to support large files
    public Response trainingGetMaterials(@Context SecurityContext securityContext,
                                         @Context ContainerRequestContext requestContext,
                                         @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = this.trainingLogic.getTrainingMaterials(securityContext, devid,
                ParameterFilter.getAiid(requestContext));

        // TODO: send out a properly formatted JSON response when we no longer use SQS.
        return Response.status(result.getStatus().getCode())
                .entity(result.getStatus().getCode() == HttpURLConnection.HTTP_OK
                        ? ((ApiTrainingMaterials) result).getTrainingFile()
                        : this.serializer.serialize(result))
                .build();
    }
}