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
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * AI Training endpoints.
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
    @ValidateParameters({APIParameter.AIID, APIParameter.TrainingSourceType})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "No file was specified; File parsing errors; Incorrect training type"),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response uploadTrainingFile(@Context ContainerRequestContext requestContext,
                                       @DefaultValue("0") @QueryParam("source_type") int type,
                                       @DefaultValue("") @QueryParam("url") String url,
                                       @FormDataParam("file") InputStream uploadedInputStream,
                                       @FormDataParam("file") FormDataContentDisposition fileDetail) {
        ApiResult result = this.trainingLogic.uploadFile(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                TrainingLogic.TrainingType.fromType(type),
                url,
                uploadedInputStream,
                fileDetail);
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/start")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "Training could not be started because it was already completed; "
                            + "A training session is already running; A training session is already queued;"
                            + "Malformed training file. Training could not be started."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response trainingStart(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.trainingLogic.startTraining(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/stop")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "AI not in an allowed state for stop training"),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response trainingStop(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.trainingLogic.stopTraining(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("/{aiid}/training/update")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST,
                    condition = "AI not in an allowed state for update training"),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response trainingUpdate(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.trainingLogic.updateTraining(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("/{aiid}/training/materials")
    @Secured({Role.ROLE_ADMIN, Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "AI not found"),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response trainingGetMaterials(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.trainingLogic.getTrainingFile(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }
}