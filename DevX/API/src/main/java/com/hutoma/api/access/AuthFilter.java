package com.hutoma.api.access;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UUID;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "authfilter";
    private final Logger logger;
    private final Config config;
    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public AuthFilter(Logger logger, Config config) {
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        try {

            // try to get the auth header
            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            // Check if the HTTP Authorization header is present and formatted correctly
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

                // not valid; tell the user to authenticate
                this.logger.logDebug(LOGFROM, "missing or invalid auth header");
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            // get the path parameters
            MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

            // Extract the token from the HTTP Authorization header
            String token = authorizationHeader.substring("Bearer".length()).trim();

            // get the encoding key to decode the token
            String encodingKey = this.config.getEncodingKey();

            // decode the token
            Claims claims = Jwts.parser().setSigningKey(encodingKey).parseClaimsJws(token).getBody();

            // get the owner devid
            String devID = claims.getSubject();
            if (devID.isEmpty()) {
                this.logger.logInfo(LOGFROM, "missing or invalid devid");
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }

            // store the decoded devid
            requestContext.getHeaders().add("_developer_id", devID);

            // is this a user token for a specific AIID?
            if (claims.containsKey("AIID")) {
                try {
                    UUID userTokenAIID = UUID.fromString(claims.get("AIID").toString());

                    // is this a request for a specific aiid?
                    if (pathParameters.containsKey("aiid")) {
                        UUID requestAIID = UUID.fromString(pathParameters.get("aiid").get(0));

                        // if request and token don't match then forbid
                        if (0 != userTokenAIID.compareTo(requestAIID)) {
                            this.logger.logInfo(LOGFROM, "aiid access denied by user token");
                            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                            return;
                        }
                    }

                } catch (IllegalFormatException ife) {
                    this.logger.logInfo(LOGFROM, "invalid aiid format");
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                    return;
                }
            }

            String devRole = claims.get("ROLE").toString();

            Class<?> resourceClass = this.resourceInfo.getResourceClass();
            List<Role> classRoles = extractRoles(resourceClass);
            Method resourceMethod = this.resourceInfo.getResourceMethod();
            List<Role> methodRoles = extractRoles(resourceMethod);

            try {
                boolean isValid;
                if (methodRoles.isEmpty()) {
                    isValid = checkPermissions(devRole, classRoles);
                } else {
                    isValid = checkPermissions(devRole, methodRoles);
                }

                if (!isValid) {
                    this.logger.logInfo(LOGFROM, "access denied for devrole to endpoint");
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                    return;
                }
            } catch (Exception e) {
                this.logger.logInfo(LOGFROM, "error checking devrole permissions");
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }

            this.logger.logDebug(LOGFROM, "authorized request from dev " + devID + " in role " + devRole);

        } catch (Exception e) {
            this.logger.logInfo(LOGFROM, "auth verification error: " + e.toString());
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            return;
        }
    }

    // Extract the roles from the annotated element
    private List<Role> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<Role>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<Role>();
            } else {
                Role[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }

    private boolean checkPermissions(String ddevRole, List<Role> allowedRoles) throws Exception {

        for (int i = 0; i < allowedRoles.size(); i++) {
            if (allowedRoles.get(i).toString().equals(ddevRole)) {
                return true;
            }
        }
        return false;
    }
}