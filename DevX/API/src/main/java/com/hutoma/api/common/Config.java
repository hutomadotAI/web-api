package com.hutoma.api.common;

import com.amazonaws.regions.Regions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config {

    private static final String LOGFROM = "config";
    private final Logger logger;
    private final HashSet<String> propertyLoaded;
    private Properties properties;

    @Inject
    public Config(Logger logger) {
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
            this.logger.logError(LOGFROM, "failed to load valid properties file: " + e.toString());
        }
    }

    public String getWnetNumberOfCPUS() {
        return getConfigFromProperties("wnet_num_CPUs", "8");
    }

    public String getEncodingKey() {
        return getConfigFromProperties("encoding_key", "");
    }

    public String getCoreQueue() {
        return getConfigFromProperties("core_queue", "");
    }

    public String getQuestionGeneratorQueue() {
        return getConfigFromProperties("sqs_QG", "");
    }

    public Regions getMessageQueueRegion() {
        return Regions.US_EAST_1;
    }

    public String getWNetServer() {
        return getConfigFromProperties("wnet_server", "");
    }

    public long getNeuralNetworkTimeout() {
        return Long.parseLong(getConfigFromProperties("rnn_timeout", "60"));
    }

    public long getMaxUploadSize() {
        return Long.parseLong(getConfigFromProperties("max_upload_size", "65536"));
    }

    public int getMaxClusterLines() {
        return Integer.parseInt(getConfigFromProperties("max_cluster_lines", "10000"));
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

    public double getClusterMinProbability() {
        return Double.parseDouble(getConfigFromProperties("cluster_min_probability", "0.7"));
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

    public String getTelemetryKey(String appName) {
        return getConfigFromProperties(String.format("telemetry_%s_key", appName), null);
    }

    private String getConfigFromProperties(String propertyName, String defaultValue) {
        if (null == this.properties) {
            this.logger.logWarning(LOGFROM, "no properties file loaded. using internal defaults where available");
            return defaultValue;
        }
        // keep a list of used properties
        // if this is the first time we are accessing a property and we are using defaults then log a warning
        if (this.propertyLoaded.add(propertyName) && (!this.properties.containsKey(propertyName))) {
            this.logger.logWarning(LOGFROM, "no property set for " + propertyName + ". using hard-coded default "
                    + defaultValue);
        }
        return this.properties.getProperty(propertyName, defaultValue);
    }

}
