package com.hutoma.api.access;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.validation.ParameterFilter;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;

@RateLimit
@Provider
@Priority(Priorities.USER) //User priority. (happens after data validation)
public class RateLimitCheck implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    Database database;
    Logger logger;
    Config config;
    JsonSerializer serializer;

    public static class RateLimitedException extends Exception {
        public RateLimitedException(String message) {
            super(message);
        }
    }

    private final String LOGFROM = "ratelimitcheck";

    @Inject
    public RateLimitCheck(Database database, Logger logger, Config config, JsonSerializer serializer) {
        this.database = database;
        this.logger = logger;
        this.config = config;
        this.serializer = serializer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        try {
            // get the bucket that we will use to rate limit this resource
            RateKey rateKey = determineRateKey(resourceInfo);

            // retrieve a validated devid, or bundle ratelimiting with anonymous
            String devid = ParameterFilter.getDevid(requestContext);
            if ((null==devid) || (devid.isEmpty())) {
                devid = "anonymous";
            }

            switch(rateKey) {
                case Chat:
                    checkRateLimitReached(devid, rateKey,
                            config.getRateLimit_Chat_BurstRequests(), config.getRateLimit_Chat_Frequency());
                    break;
                case QuickRead:
                    checkRateLimitReached(devid, rateKey,
                            config.getRateLimit_QuickRead_BurstRequests(), config.getRateLimit_QuickRead_Frequency());
                    break;
                case None:
                default:
                    break;
            }
        } catch (RateLimitedException rle) {
            requestContext.abortWith(ApiError.getRateLimited().getResponse(serializer).build());
            logger.logInfo(LOGFROM, rle.getMessage());
        } catch (Exception e) {
            requestContext.abortWith(ApiError.getInternalServerError().getResponse(serializer).build());
            logger.logError(LOGFROM, e.toString());
        }
    }

    /***
     * Method has priority over class for rate limit bucket definitions
     * @param resourceInfo
     * @return the rate key to use
     */
    private RateKey determineRateKey(ResourceInfo resourceInfo) {
        RateKey rateKey = extractRateLimitBucket(resourceInfo.getResourceMethod());
        if (null == rateKey) {
            rateKey = extractRateLimitBucket(resourceInfo.getResourceClass());
        }
        return (null==rateKey)? RateKey.None:rateKey;
    }

    /***
     * Checks for rate limiting. Throws an exception if this call should fail
     * @param devid
     * @param rateKey
     * @param burst rate limiting param
     * @param frequency rate limiting param
     * @throws Database.DatabaseException if db call fails
     * @throws RateLimitedException if we should fail the call due to limiting
     */
    private void checkRateLimitReached(String devid, RateKey rateKey, double burst, double frequency) throws Database.DatabaseException, RateLimitedException {
        RateLimitStatus rateLimitStatus = database.checkRateLimit(devid, rateKey.toString(), burst, frequency);
        if (rateLimitStatus.isRateLimited()) {
            long blockedFor = Math.round(1000.0d * (1.0d - rateLimitStatus.getTokens()) * frequency);
            throw new RateLimitedException(devid + " hit limit on " + rateKey.toString() + ". BLOCKED for the next " + blockedFor + "ms.");
        }
        logger.logDebug(LOGFROM, "OK for " + rateKey.toString() + " with " + rateLimitStatus.getTokens() + " tokens remaining.");
    }

    /***
     * Extract the rate key from the annotated element
     */
    private RateKey extractRateLimitBucket(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            RateLimit limited = annotatedElement.getAnnotation(RateLimit.class);
            if (limited != null) {
                 return limited.value();
            }
        }
        return null;
    }

}
