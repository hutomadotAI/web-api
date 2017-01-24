package com.hutoma.api.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config {

    private static final String LOGFROM = "config";
    private static final String API_ENV_PREFIX = "API_";
    private final ILogger logger;
    private final HashSet<String> propertyLoaded;
    private Properties properties;

    @Inject
    public Config(ILogger logger) {
        this.logger = logger;
        this.propertyLoaded = new HashSet<>();
        loadPropertiesFile();
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

    public void loadPropertiesFile() {
        this.propertyLoaded.clear();
        Path configPath = Paths.get(System.getProperty("user.home"), "/ai/v1.config.properties");

        try (InputStream fileInputStream = new FileInputStream(configPath.toFile())) {
            Properties loadProperties = new Properties();
            loadProperties.load(fileInputStream);
            this.properties = loadProperties;
            this.logger.logInfo(LOGFROM, "loaded " + this.properties.size() + " properties file from "
                    + configPath.toString());
        } catch (IOException e) {
            this.logger.logWarning(LOGFROM, "failed to load valid properties file: " + e.toString());
        }
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

    public String getWnetTrainingEndpoint() {
        return getConfigFromProperties("ai_wnet_endpoint", "");
    }

    public String getRnnTrainingEndpoint() {
        return getConfigFromProperties("ai_rnn_endpoint", "");
    }

    public String getWnetChatEndpoint() {
        return getConfigFromProperties("ai_wnet_endpoint", "");
    }

    public String getAimlChatEndpoint() {
        return getConfigFromProperties("ai_aiml_endpoint", "");
    }

    public String getRnnChatEndpoint() {
        return getConfigFromProperties("ai_rnn_endpoint", "");
    }

    public String getLoggingServiceUrl() {
        return getConfigFromProperties("logging_url", null);
    }

    public int getLoggingUploadCadency() {
        return Integer.parseInt(getConfigFromProperties("logging_cadency", "5000"));
    }

    public void dumpApiEnvironmentVars() {
        System.getenv().entrySet().stream().forEach(e -> {
            if (e.getKey().startsWith(API_ENV_PREFIX)) {
                this.logger.logInfo(LOGFROM, e.getKey() + "=" + e.getValue());
            }
        });
    }

    public void validateConfigPresent() throws Exception {
        // Validate encoding key is present otherwise we can't sign
        if (this.getEncodingKey() == null || this.getEncodingKey().isEmpty()) {
            throw new Exception("Encoding key hasn't been defined");
        }
    }

    private List<String> getCSList(final String propertyName) {
        String instances = getConfigFromProperties(propertyName, null);
        if (instances != null && !instances.isEmpty()) {
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