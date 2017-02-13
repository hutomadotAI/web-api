package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.logic.DeveloperInfoLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Developer endpoint.
 */
@Path("/developer/")
public class DeveloperInfoEndpoint {

    private final DeveloperInfoLogic developerLogic;
    private final JsonSerializer serializer;

    @Inject
    public DeveloperInfoEndpoint(final DeveloperInfoLogic developerLogic, final JsonSerializer serializer) {
        this.developerLogic = developerLogic;
        this.serializer = serializer;
    }

    /**
     * Gets the developer info.
     * @return the developer info
     */
    @GET
    @Path("{devid}")
    @RateLimit(RateKey.QuickRead)
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Developer not found")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public
    @TypeHint(DeveloperInfo.class)
    Response getDeveloperInfo(
            @Context final ContainerRequestContext requestContext,
            @NotNull final @PathParam("devid") String requestDevId) {
        ApiResult result = this.developerLogic.getDeveloperInfo(
                ParameterFilter.getDevid(requestContext),
                requestDevId
        );
        return result.getResponse(this.serializer).build();
    }

    /**
     * Sets the developer information.
     * @param requestContext the request context
     * @param name           the name
     * @param company        the company name
     * @param email          the email address
     * @param address        the mailing address
     * @param postCode       the post code address
     * @param city           the city address
     * @param country        the country address
     * @param website        the web site
     * @return the response
     */
    @POST
    @Path("{devid}")
    @RateLimit(RateKey.QuickRead)
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_BAD_REQUEST, condition = "Developer info already submitted."),
            @ResponseCode(code = HttpURLConnection.HTTP_NOT_FOUND, condition = "Developer id not found.")
    })
    @RequestHeaders({
            @RequestHeader(name = "Authorization", description = "Developer token")
    })
    public Response postDeveloperInfo(
            @Context final ContainerRequestContext requestContext,
            @NotNull final @FormParam("name") String name,
            @NotNull final @FormParam("company") String company,
            @NotNull final @FormParam("email") String email,
            @NotNull final @FormParam("address") String address,
            @NotNull final @FormParam("postCode") String postCode,
            @NotNull final @FormParam("city") String city,
            @NotNull final @FormParam("country") String country,
            @DefaultValue("") final @FormParam("website") String website) {

        ApiResult result = this.developerLogic.setDeveloperInfo(
                ParameterFilter.getDevid(requestContext),
                name,
                company,
                email,
                address,
                postCode,
                city,
                country,
                website
        );
        return result.getResponse(this.serializer).build();
    }
}
