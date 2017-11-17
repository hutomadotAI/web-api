package com.hutoma.api.common;

import com.hutoma.api.connectors.db.IDatabaseConfig;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.ILoggerConfig;
import com.hutoma.api.thread.IThreadConfig;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config extends CommonConfig implements ILoggerConfig, IThreadConfig, IDatabaseConfig {

    private static final String LOGFROM = "config";


    @Inject
    public Config(final ILogger logger) {
        super(logger);
    }

    @Override
    protected String getLoggingLogfrom() {
        return LOGFROM;
    }

    @Override
    protected String getEnvPrefix() {
        return "API_";
    }



    public String getEncodingKey() {
        return getConfigFromProperties("encoding_key", "");
    }

    public int getMaxUploadSizeKb() {
        return Integer.parseInt(getConfigFromProperties("max_upload_size_kb", "4096"));
    }

    @Override
    public String getDatabaseConnectionString() {
        // if we are using admin or root, log an error and return an empty connection string
        try {
            return IDatabaseConfig.enforceNewDBCredentials(getConfigFromProperties("connection_string", ""));
        } catch (Exception e) {
            this.logger.logError(LOGFROM, e.getMessage());
        }
        return "";
    }

    @Override
    public int getDatabaseConnectionPoolMinimumSize() {
        return Integer.parseInt(getConfigFromProperties("dbconnectionpool_min_size", "8"));
    }

    @Override
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
    @Override
    public int getThreadPoolMaxThreads() {
        return 1024;
    }

    /***
     * The time after which an idle thread in the thread pool get be closed
     * @return
     */
    @Override
    public long getThreadPoolIdleTimeMs() {
        return 60 * 1000;
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

    @Override
    public String getFluentLoggingHost() {
        return getConfigFromProperties("logging_fluent_host", "log-fluent");
    }

    @Override
    public int getFluentLoggingPort() {
        return Integer.parseInt(getConfigFromProperties("logging_fluent_port", "24224"));
    }

    public String getElasticSearchAnalyticsUrl() {
        return getConfigFromProperties("analytics_es_url", "");
    }

    public String getEntityRecognizerUrl() {
        return getConfigFromProperties("entity_recognizer_url", "");
    }

    public String getControllerEndpoint() { return getConfigFromProperties("controller_url", "http://localhost:8080/v1"); }

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

}
