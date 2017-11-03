package com.hutoma.api.common;

import com.hutoma.api.connectors.db.IDatabaseConfig;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.thread.IThreadConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config implements ILoggerConfig, IThreadConfig, IDatabaseConfig {

    private static final String LOGFROM = "config";
    private static final String API_ENV_PREFIX = "API_";
    private final ILogger logger;
    private final HashSet<String> propertyLoaded;
    private Properties properties;

    @Inject
    public Config(ILogger logger) {
        this.logger = logger;
        this.propertyLoaded = new HashSet<>();
    }

    /***
     * Throw an exception if we are using a username that starts with 'admin' or 'root'
     * @param value connection string
     * @return same connection string
     * @throws Exception if username is admin... or root...
     */
    private static String enforceNewDBCredentials(String value) throws Exception {
        int startUserName = value.indexOf("user=");
        if (startUserName >= 0) {
            String prefixUsername = value.substring(startUserName + ("user=".length())).toLowerCase();
            if ((prefixUsername.startsWith("admin")) || (prefixUsername.startsWith("root"))) {
                throw new Exception(
                        "db connection string uses root/admin access. please update your config properties file.");
            }
        }
        return value;
    }

    public String getEncodingKey() {
        return getConfigFromProperties("encoding_key", "");
    }

    public int getMaxUploadSizeKb() {
        return Integer.parseInt(getConfigFromProperties("max_upload_size_kb", "4096"));
    }

    public String getDatabaseConnectionString() {
        // if we are using admin or root, log an error and return an empty connection string
        try {
            return enforceNewDBCredentials(getConfigFromProperties("connection_string", ""));
        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage());
        }
        return "";
    }

    public int getDatabaseConnectionPoolMinimumSize() {
        return Integer.parseInt(getConfigFromProperties("dbconnectionpool_min_size", "8"));
    }

    public int getDatabaseConnectionPoolMaximumSize() {
        return Integer.parseInt(getConfigFromProperties("dbconnectionpool_max_size", "64"));
    }

    public double getRateLimit_Chat_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_chat_burst", "3.0"));
    }

    public double getRateLimit_Chat_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_chat_frequency", "2.0"));
    }

    public double getRateLimit_QuickRead_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_quickread_burst", "5.0"));
    }

    public double getRateLimit_QuickRead_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_quickread_frequency", "0.5"));
    }

    public double getRateLimit_LoadTest_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_loadtest_burst", "32768.0"));
    }

    public double getRateLimit_LoadTest_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_loadtest_frequency", "8192.5"));
    }

    public double getRateLimit_BotstoreMetadata_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_botstore_metadata_burst", "20.0"));
    }

    public double getRateLimit_BotstoreMetadata_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_botstore_metadata_frequency", "2.0"));
    }

    public double getRateLimit_BotstorePublish_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_botstore_publish_burst", "4.0"));
    }

    public double getRateLimit_BotstorePublish_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_botstore_publish_frequency", "120.0"));
    }

    public double getRateLimit_Analytics_BurstRequests() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_analytics_burst", "4.0"));
    }

    public double getRateLimit_Analytics_Frequency() {
        return Double.parseDouble(getConfigFromProperties("ratelimit_analytics_frequency", "5.0"));
    }

    public List<String> getAutoPurchaseBotIds() {
        List<String> stringList = getCSList("auto_purchase_botlist");
        return stringList == null ? Collections.emptyList() : stringList;
    }

    public List<UUID> getAimlBotAiids() {
        List<String> stringList = getCSList("ai_aiml_bot_aiids");
        if (stringList == null) {
            stringList = Arrays.asList("e1bb8226-e8ce-467a-8305-bc2fcb89dd7f");
        }
        return stringList.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public int getMaxLinkedBotsPerAi() {
        return Integer.parseInt(getConfigFromProperties("max_linked_bots_per_ai", "5"));
    }

    /***
     * Gets the AppID for the hu:toma facebook app that we are using to integrate chat
     * The default value links to a private, developer only app called hu:toma-test
     * https://developers.facebook.com/apps/843873759109923
     * @return
     */
    public String getFacebookAppId() {
        return getConfigFromProperties("fb_app_id", "843873759109923");
    }

    /***
     * Gets the AppID secret key for the facebook app that we are using to integrate chat
     * The default key belongs to the test AppID
     * https://developers.facebook.com/apps/843873759109923
     * @return
     */
    public String getFacebookAppSecret() {
        return getConfigFromProperties("fb_app_secret", "fe63413659258d447bff34c048978d9d");
    }

    /***
     * How many ms to wait before giving up on Facebook
     * @return
     */
    public int getFacebookGraphAPITimeout() {
        return 10000;
    }

    /***
     * How many ms to wait before giving up on Facebook
     * when we are sending a message using Send API
     * @return
     */
    public int getFacebookSendAPITimeout() {
        return 60000;
    }

    /***
     * The maximum number of active threads in the threadpool
     * after which anyone requesting a thread will get an exception
     * @return
     */
    public int getThreadPoolMaxThreads() {
        return 1024;
    }

    /***
     * The time after which an idle thread in the thread pool get be closed
     * @return
     */
    public long getThreadPoolIdleTimeMs() {
        return 60 * 1000;
    }

    /***
     * Under normal conditions the controller will ping the server every n milliseconds
     * @return n
     */
    public long getServerHeartbeatEveryMs() {
        return 2 * 1000;
    }

    /***
     * However long the last call took, always wait a minimum of n milliseconds
     * before issuing the next ping
     * i.e. if we issue a ping every 2 seconds and the ping takes 2 seconds to complete
     * we would still wait n ms between calls
     * @return n
     */
    public long getServerHeartbeatMinimumGapMs() {
        return 500;
    }

    /***
     * If we haven't received a valid ping for n milliseconds
     * then we write off this server and it has to re-register with us
     * @return n
     */
    public long getServerHeartbeatFailureCutOffMs() {
        return 5 * 1000;
    }

    /***
     * Every n milliseconds we check the queue status to see
     * if there are tasks to run or reschedule
     * @return
     */
    public long getProcessQueueIntervalDefault() {
        return 2 * 1000;
    }

    /***
     * The time to wait if a command needs to be scheduled
     * immediately after this one (in ms)
     * i.e. minimum interval between queue checks
     * @return
     */
    public long getProcessQueueIntervalShort() {
        return 1000;
    }

    /***
     * The time to wait if nothing much is going on
     * and we can wait a while before checking the queue again
     * @return
     */
    public long getProcessQueueIntervalLong() {
        return 10 * 1000;
    }

    /***
     * How far in the future to schedule a command
     * (in seconds)
     */
    public int getProcessQueueScheduleFutureCommand() {
        return 30;
    }

    /***
     * If this many seconds pass and no update is received for an active training slot
     * then we consider it 'interrupted' and reallocate the training job to a server with space
     */
    public int getProcessQueueInterruptedSeconds() {
        return 2 * 60;
    }

    /***
     * Do not attempt slot recovery for the first n seconds after the API has started up
     * This gives servers enough time to re-register and reclaim their training tasks
     */
    public int getProcessQueueDelayRecoveryForFirstSeconds() {
        return 2 * 60;
    }

    /***
     * The total number of milliseconds that we wait for backend
     * requests to complete.
     * N.B. this value is not 'per request'.
     * If we start the first call at t=1 then we won't wait beyond t=20001
     * @return
     */
    public long getBackendCombinedRequestTimeoutMs() {
        return Long.parseLong(getConfigFromProperties("backend_request_timeout_ms", "20000"));
    }

    /**
     * The botstore icon storage path. This will typically be a NFS mount point.
     * @return the botstore icon storage path
     */
    public String getBotIconStoragePath() {
        return getConfigFromProperties("bot_icon_path", "/boticon");
    }

    public String getFluentLoggingHost() {
        return getConfigFromProperties("logging_fluent_host", "log-fluent");
    }

    public int getFluentLoggingPort() {
        return Integer.parseInt(getConfigFromProperties("logging_fluent_port", "24224"));
    }

    public String getElasticSearchAnalyticsUrl() {
        return getConfigFromProperties("analytics_es_url", "");
    }

    public String getEntityRecognizerUrl() {
        return getConfigFromProperties("entity_recognizer_url", "");
    }

    public void validateConfigPresent() throws Exception {
        // Validate encoding key is present otherwise we can't sign
        if (this.getEncodingKey() == null || this.getEncodingKey().isEmpty()) {
            throw new Exception("Encoding key hasn't been defined");
        }
    }

    /***
     * The string that we expect Facebook to pass to us when we are verifying that a webhook endpoint is valid
     * This is typically needed only once when we manually link the Hutoma Facebook App to a webhook
     * @return
     */
    public String getFacebookVerifyToken() {
        return getConfigFromProperties("fb_verify_token", "oYfoYghfwj1p0i7f");
    }

    private List<String> getCSList(final String propertyName) {
        String instances = getConfigFromProperties(propertyName, null);
        if (instances == null) {
            return null;
        }
        if (!instances.isEmpty()) {
            return Arrays.asList(instances.split(","));
        }
        return new ArrayList<>();
    }

    private String getConfigFromEnvironment(String propertyName) {
        return System.getenv(API_ENV_PREFIX + propertyName.toUpperCase());
    }

    private String getConfigFromProperties(String propertyName, String defaultValue) {
        String configFromEnv = getConfigFromEnvironment(propertyName);
        if (configFromEnv != null && !configFromEnv.isEmpty()) {
            return configFromEnv;
        }

        if (this.properties == null) {
            // if this is the first time we are accessing a property and we are using defaults then log a warning
            if (this.propertyLoaded.add(propertyName)) {
                if (defaultValue == null || defaultValue.isEmpty()) {
                    this.logger.logWarning(LOGFROM, String.format("No value found for property \"%s\"!", propertyName));
                } else {
                    this.logger.logWarning(LOGFROM, String.format(
                            "No value found for property \"%s\". Using default value \"%s\"",
                            propertyName, defaultValue));
                }
                this.propertyLoaded.add(propertyName);

            }
            return defaultValue;
        } else {
            if (!this.properties.containsKey(propertyName)) {
                this.logger.logWarning(LOGFROM, "no property set for " + propertyName + ". using hard-coded default "
                        + defaultValue);
            }
            return this.properties.getProperty(propertyName, defaultValue);
        }
    }

}