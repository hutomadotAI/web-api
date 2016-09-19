package com.hutoma.api.access;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import io.jsonwebtoken.Jwts;

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
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private Logger logger;
    private Config config;

    @Inject
    public AuthFilter(Logger logger, Config config) {
        this.logger = logger;
        this.config = config;
    }

    private final String LOGFROM = "authfilter";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        logger.logDebug(LOGFROM, "endpoint secured");

        try {

            String encoding_key = config.getEncodingKey();

            String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String _devrole = "";
            String _aiid = "";
            String _devid ="";
            MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

            // Check if the HTTP Authorization header is present and formatted correctly
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }

            String requested_aiid="";
            try {
                requested_aiid = pathParameters.get("aiid").get(0);
            }
            catch (Exception e) {}

            // Extract the token from the HTTP Authorization header
            String token = authorizationHeader.substring("Bearer".length()).trim();

            try {
                _devid =Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(token).getBody().getSubject().toString();
                requestContext.getHeaders().add("_developer_id",_devid);
                if (_devid.isEmpty()) requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());

                try {
                    _aiid = "" + Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(token).getBody().get("AIID").toString();
                    if (!requested_aiid.equals(_aiid))
                        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                }
                catch (Exception ex) {}

                _devrole = Jwts.parser().setSigningKey(encoding_key).parseClaimsJws(token).getBody().get("ROLE").toString();
                logger.logDebug(LOGFROM, "devrole " + _devrole);
            } catch (Exception e) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }

            Class<?> resourceClass = resourceInfo.getResourceClass();
            List<Role> classRoles = extractRoles(resourceClass);
            Method resourceMethod = resourceInfo.getResourceMethod();
            List<Role> methodRoles = extractRoles(resourceMethod);

            try {
                boolean is_valid;
                if (methodRoles.isEmpty()) {
                    is_valid = checkPermissions(_devrole, classRoles);
                } else {
                    is_valid = checkPermissions(_devrole, methodRoles);
                }

                if (!is_valid) {
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                }
            } catch (Exception e) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
        catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
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

    private boolean checkPermissions(String dev_role,List<Role> allowedRoles) throws Exception {

        for (int i =0;i<allowedRoles.size();i++) {
         if (allowedRoles.get(i).toString().equals(dev_role)) return true;
        }
        return false;
    }
}