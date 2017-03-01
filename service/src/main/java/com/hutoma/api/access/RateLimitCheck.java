package com.hutoma.api.access;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.validation.ParameterFilter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@RateLimit
@Provider
@Priority(Priorities.USER) //User priority. (happens after data validation)
public class RateLimitCheck implements ContainerRequestFilter {

    private static final String LOGFROM = "ratelimitcheck";
    Database database;
    ILogger logger;
    Config config;
    JsonSerializer serializer;
    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public RateLimitCheck(Database database, ILogger logger, Config config, JsonSerializer serializer) {
        this.database = database;
        this.logger = logger;
        this.config = config;
        this.serializer = serializer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // retrieve a validated devid, or bundle ratelimiting with anonymous
        String devid = ParameterFilter.getDevid(requestContext);

        try {
            if ((null == devid) || (devid.isEmpty())) {
                devid = "{null or empty}";
                throw new AccountDisabledException();
            }

            // get the bucket that we will use to rate limit this resource
            RateKey rateKey = determineRateKey(this.resourceInfo);

            switch (rateKey) {
                case Chat:
                    checkRateLimitReached(devid, rateKey,
                            this.config.getRateLimit_Chat_BurstRequests(), this.config.getRateLimit_Chat_Frequency());
                    break;
                case QuickRead:
                    checkRateLimitReached(devid, rateKey,
                            this.config.getRateLimit_QuickRead_BurstRequests(),
                            this.config.getRateLimit_QuickRead_Frequency());
                    break;
                case LoadTest:
                    checkRateLimitReached(devid, rateKey,
                            this.config.getRateLimit_LoadTest_BurstRequests(),
                            this.config.getRateLimit_LoadTest_Frequency());
                    break;
                case Botstore_Metadata:
                    checkRateLimitReached(devid, rateKey,
                            this.config.getRateLimit_BotstoreMetadata_BurstRequests(),
                            this.config.getRateLimit_BotstoreMetadata_Frequency());
                    break;
                case Botstore_Publish:
                    checkRateLimitReached(devid, rateKey,
                            this.config.getRateLimit_BotstorePublish_BurstRequests(),
                            this.config.getRateLimit_BotstorePublish_Frequency());
                    break;
                case None:
                default:
                    break;
            }
        } catch (AccountDisabledException ade) {
            requestContext.abortWith(ApiError.getAccountDisabled().getResponse(this.serializer).build());
            this.logger.logInfo(LOGFROM, String.format("denying access to invalid account for devid %s", devid));
        } catch (RateLimitedException rle) {
            requestContext.abortWith(ApiError.getRateLimited().getResponse(this.serializer).build());
            this.logger.logInfo(LOGFROM, rle.getMessage());
        } catch (Exception e) {
            requestContext.abortWith(ApiError.getInternalServerError().getResponse(this.serializer).build());
            this.logger.logError(LOGFROM, e.toString());
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
        return (null == rateKey) ? RateKey.None : rateKey;
    }

    /***
     * Checks for rate limiting. Throws an exception if this call should fail
     * @param devid
     * @param rateKey
     * @param burst rate limiting param
     * @param frequency rate limiting param
     * @throws Database.DatabaseException if db call fails
     * @throws RateLimitedException if we should fail the call due to limiting
     * @throws AccountDisabledException if the devid was not recognised or the account was disabled
     */
    private void checkRateLimitReached(String devid, RateKey rateKey, double burst, double frequency)
            throws Database.DatabaseException, RateLimitedException, AccountDisabledException {
        RateLimitStatus rateLimitStatus = this.database.checkRateLimit(devid, rateKey.toString(), burst, frequency);
        if (!rateLimitStatus.isAccountValid()) {
            throw new AccountDisabledException();
        }
        if (rateLimitStatus.isRateLimited()) {
            long blockedFor = Math.round(1000.0d * (1.0d - rateLimitStatus.getTokens()) * frequency);
            throw new RateLimitedException(devid + " hit limit on " + rateKey.toString() + ". BLOCKED for the next "
                    + blockedFor + "ms.");
        }
        this.logger.logDebug(LOGFROM, "OK for " + rateKey.toString() + " with " + rateLimitStatus.getTokens()
                + " tokens remaining.");
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

    public static class RateLimitedException extends Exception {
        public RateLimitedException(String message) {
            super(message);
        }
    }

    public static class AccountDisabledException extends Exception {
    }
}
