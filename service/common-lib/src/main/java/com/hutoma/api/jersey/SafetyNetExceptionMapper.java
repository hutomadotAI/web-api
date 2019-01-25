package com.hutoma.api.jersey;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.hutoma.api.logging.ILogger;

import org.glassfish.jersey.spi.ExtendedExceptionMapper;

@javax.ws.rs.ext.Provider
public class SafetyNetExceptionMapper implements ExtendedExceptionMapper<Exception> {
    private static final String LOGFROM = "jersey-safety-net";

    @Inject
    private ILogger logger;
    
    @Override
    public boolean isMappable(Exception exception) {
        // Any explicit WebApplicationException was intentional from upstream.
        // We don't need to catch them in the safety net
        return !(exception instanceof WebApplicationException);
    }

    @Override
    public Response toResponse(Exception exception) {
        logger.logException(LOGFROM, exception);
        return Response.status(500).entity("Internal Server Error").type("text/plain")
                .build();
    }
}
