package com.hutoma.api.access;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.RateLimitStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
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
import java.util.UUID;

@RateLimit
@Provider
@Priority(Priorities.USER) //User priority. (happens after data validation)
public class RateLimitCheck implements ContainerRequestFilter {

    private static final String LOGFROM = "ratelimitcheck";
    private final Database database;
    private final ILogger logger;
    private final Config config;
    private final JsonSerializer serializer;
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
        String devIdString = "{null or empty}";
        UUID devid = ParameterFilter.getDevid(requestContext);
        LogMap logMap = LogMap.map("Method", requestContext.getMethod())
                .put("Path", requestContext.getUriInfo().getPath());

        double burst = 0.0;
        double frequency = 1.0;
        boolean skipRateLimit = false;
        try {
            if (null == devid) {
                throw new AccountDisabledException();
            }

            devIdString = devid.toString();
            // get the bucket that we will use to rate limit this resource
            RateKey rateKey = determineRateKey(this.resourceInfo);

            switch (rateKey) {
                case Chat:
                    burst = this.config.getRateLimit_Chat_BurstRequests();
                    frequency = this.config.getRateLimit_Chat_Frequency();
                    break;
                case QuickRead:
                    burst = this.config.getRateLimit_QuickRead_BurstRequests();
                    frequency = this.config.getRateLimit_QuickRead_Frequency();
                    break;
                case SaveResource:
                    burst = this.config.getRateLimit_SaveResource_BurstRequests();
                    frequency = this.config.getRateLimit_SaveResource_Frequency();
                    break;
                case PollStatus:
                    burst = this.config.getRateLimit_PollStatus_BurstRequests();
                    frequency = this.config.getRateLimit_PollStatus_Frequency();
                    break;
                case LoadTest:
                    burst = this.config.getRateLimit_LoadTest_BurstRequests();
                    frequency = this.config.getRateLimit_LoadTest_Frequency();
                    break;
                case Botstore_Metadata:
                    burst = this.config.getRateLimit_BotstoreMetadata_BurstRequests();
                    frequency = this.config.getRateLimit_BotstoreMetadata_Frequency();
                    break;
                case Botstore_Publish:
                    burst = this.config.getRateLimit_BotstorePublish_BurstRequests();
                    frequency = this.config.getRateLimit_BotstorePublish_Frequency();
                    break;
                case Analytics:
                    burst = this.config.getRateLimit_Analytics_BurstRequests();
                    frequency = this.config.getRateLimit_Analytics_Frequency();
                    break;
                case None:
                    skipRateLimit = true;
                    break;
                default:
                    break;
            }

            if (!skipRateLimit) {
                logMap.add("RateKey", rateKey.toString());
                checkRateLimitReached(devid, rateKey, burst, frequency);
            }

        } catch (AccountDisabledException ade) {
            requestContext.abortWith(ApiError.getAccountDisabled().getResponse(this.serializer).build());
            this.logger.logUserTraceEvent(LOGFROM, "Account not valid", devIdString, logMap);
        } catch (RateLimitedException rle) {
            requestContext.abortWith(ApiError.getRateLimited().getResponse(this.serializer).build());
            this.logger.logUserTraceEvent(LOGFROM, rle.getMessage(), devIdString,
                    logMap.put("Burst", burst).put("Frequency", frequency));
        } catch (Exception e) {
            requestContext.abortWith(ApiError.getInternalServerError().getResponse(this.serializer).build());
            this.logger.logUserExceptionEvent(LOGFROM, e.toString(), devIdString, e);
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
     * @throws DatabaseException if db call fails
     * @throws RateLimitedException if we should fail the call due to limiting
     * @throws AccountDisabledException if the devid was not recognised or the account was disabled
     */
    private void checkRateLimitReached(final UUID devid, final RateKey rateKey, final double burst,
                                       final double frequency)
            throws DatabaseException, RateLimitedException, AccountDisabledException {
        if (frequency == 0.0d) {
            throw new IllegalArgumentException("Frequency cannot be zero");
        }
        RateLimitStatus rateLimitStatus = this.database.checkRateLimit(devid, rateKey.toString(), burst, frequency);

        if (!rateLimitStatus.isAccountValid()) {
            throw new AccountDisabledException();
        }
        if (rateLimitStatus.isRateLimited()) {
            long blockedFor = Math.round(1000.0d * (1.0d - rateLimitStatus.getTokens()) * frequency);
            throw new RateLimitedException(String.format("Limit hit for %s. Blocked for %d ms",
                    rateKey.toString(), blockedFor));
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
