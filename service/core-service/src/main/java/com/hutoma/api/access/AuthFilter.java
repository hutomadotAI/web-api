package com.hutoma.api.access;

import com.hutoma.api.common.AccessLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.logging.LogMap;
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
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
    private final AccessLogger logger;
    private final Config config;
    @Context
    private ResourceInfo resourceInfo;
    @Context
    private HttpServletRequest servletRequest;

    @Inject
    public AuthFilter(final AccessLogger logger, final Config config) {
        this.logger = logger;
        this.config = config;
    }

    /**
     * Retrieves the token from the 'Bearer' authentication header.
     * @param requestContext the request context
     * @return the token, or null if not present
     */
    private static String getTokenFromAuthBearer(final ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer".length()).trim();
        }
        return null;
    }

    /**
     * Gets the claims from the JWT token.
     * @param token  the token as a string
     * @param config the configuration
     * @return the claims
     */
    private static Claims getClaimsFromToken(final String token, final Config config) {
        // get the encoding key to decode the token
        String encodingKey = config.getEncodingKey();
        // decode the token
        return Jwts.parser().setSigningKey(encodingKey).parseClaimsJws(token).getBody();
    }

    /**
     * Retrieves the developer ID from the header.
     * @param requestContext the request context
     * @param config         the configuration
     * @return the developer id, or null if not present
     */
    public static UUID getDevIdFromHeader(final ContainerRequestContext requestContext, final Config config) {
        String token = getTokenFromAuthBearer(requestContext);
        if (token != null) {
            final String devId = getClaimsFromToken(token, config).getSubject();
            return UUID.fromString(devId);
        }
        return null;
    }

    /**
     * Filter handler.
     * @param requestContext the request context
     * @throws IOException if an IO exception occurs
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        try {

            String token = getTokenFromAuthBearer(requestContext);
            if (token == null) {
                // not valid; tell the user to authenticate
                this.logger.logDebug(LOGFROM, "missing or invalid auth header");
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            // get the path parameters
            MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

            // decode the token
            Claims claims = getClaimsFromToken(token, this.config);

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
                boolean isAllowed = methodRoles.isEmpty()
                        ? isAllowed(devRole, classRoles) : isAllowed(devRole, methodRoles);

                if (!isAllowed) {
                    this.logger.logInfo(LOGFROM, "access denied for devrole to endpoint");
                    requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                    return;
                }
            } catch (Exception e) {
                this.logger.logInfo(LOGFROM, "error checking devrole permissions");
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }

            String pathClass = resourceClass.getAnnotation(Path.class) != null
                    ? resourceClass.getAnnotation(Path.class).value() : "";
            String pathMethod = resourceMethod.getAnnotation(Path.class) != null
                    ? resourceMethod.getAnnotation(Path.class).value() : "";
            String forwardedHeader = this.servletRequest.getHeader("X-Forwarded-For");
            LogMap logMap = LogMap
                    .map("Role", devRole)
                    .put("Method", this.servletRequest.getMethod())
                    .put("QueryString", this.servletRequest.getQueryString() != null
                            ? this.servletRequest.getQueryString() : "")
                    .put("URI", this.servletRequest.getRequestURI())
                    .put("RemoteAddr", this.servletRequest.getRemoteAddr())
                    .put("PathClass", pathClass)
                    .put("PathMethod", pathMethod)
                    .put("Path", String.format("%s/%s", pathClass, pathMethod).replaceAll("(\\/)\\/+", "/"))
                    .put("X-Forwarded-For", forwardedHeader != null ? forwardedHeader : "");

            this.logger.logUserTraceEvent(LOGFROM, "apiCall", devID, logMap);

        } catch (Exception e) {
            this.logger.logInfo(LOGFROM, "auth verification error: " + e.toString());
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    /**
     * Extract the roles from the annotated element.
     * @param annotatedElement the annotated element
     * @return the list of roles (or an empty list if there are none)
     */
    private List<Role> extractRoles(final AnnotatedElement annotatedElement) {
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

    /**
     * Checks if the given role is in the allowed roles
     * @param devRole      the role
     * @param allowedRoles list of allowed roles
     * @return whether the given role is in the allowed roles or not
     */
    private boolean isAllowed(final String devRole, final List<Role> allowedRoles) {
        return allowedRoles.stream().anyMatch(x -> x.toString().equals(devRole));
    }
}
