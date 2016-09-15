package com.hutoma.api.common;

import com.amazonaws.regions.Regions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config {

    Logger logger;
    Properties properties;

    private final String LOGFROM = "config";

    @Inject
    public Config(Logger logger) {
        this.logger = logger;
        loadPropertiesFile();
    }

    public void loadPropertiesFile() {
        Path configPath = Paths.get(System.getProperty("user.home"), "/ai/v1.config.properties");
        try {
            Properties loadProperties = new Properties();
            loadProperties.load(new FileInputStream(configPath.toFile()));
            properties = loadProperties;
            logger.logInfo(LOGFROM, "loaded " + properties.size() + " properties file from " + configPath.toString());
        } catch (IOException e) {
            logger.logError(LOGFROM, "failed to load valid properties file: " + e.toString());
        }
    }

    public  String getWnetNumberOfCPUS() {
        return getConfigFromProperties("num_CPUs", "8");
    }

    public String getEncodingKey() {
        return getConfigFromProperties("encoding_key", "");
    }

    public String getCoreQueue() {
        return getConfigFromProperties("core_queue", "");
    }

    public Regions getMessageQueueRegion() {
        return Regions.US_EAST_1;
    }

    public String getWNetServer() {
        return getConfigFromProperties("wnet_server", "");
    }

    public long getNeuralNetworkTimeout() {
        return Long.valueOf(getConfigFromProperties("RNNTimeout", "60"));
    }

    public long getMaxUploadSize() {
        return Long.valueOf(getConfigFromProperties("MaxUploadSize", "65536"));
    }

    public int getMaxClusterLines() {
        return Integer.valueOf(getConfigFromProperties("max_cluster_lines", "10000"));
    }

    public String getDatabaseConnectionString() {
        return enforceNewDBCredentials(getConfigFromProperties("connectionstring", ""));
    }

    public int getDatabaseConnectionPoolMinimumSize() {
        return Integer.valueOf(getConfigFromProperties("DBConnectionPoolMinSize", "8"));
    }

    public int getDatabaseConnectionPoolMaximumSize() {
        return Integer.valueOf(getConfigFromProperties("DBConnectionPoolMaxSize", "64"));
    }

    public double getClusterMinProbability() {
        return Double.valueOf(getConfigFromProperties("cluster_min_probability", "0.7"));
    }

    public double getRateLimit_Chat_BurstRequests() {
        return Double.valueOf(getConfigFromProperties("ratelimit_chat_burst", "3.0"));
    }

    public double getRateLimit_Chat_Frequency() {
        return Double.valueOf(getConfigFromProperties("ratelimit_chat_frequency", "2.0"));
    }

    public double getRateLimit_QuickRead_BurstRequests() {
        return Double.valueOf(getConfigFromProperties("ratelimit_quickread_burst", "5.0"));
    }

    public double getRateLimit_QuickRead_Frequency() {
        return Double.valueOf(getConfigFromProperties("ratelimit_quickread_frequency", "0.5"));
    }
    private String getConfigFromProperties(String p, String defaultValue) {
        if (null==properties) {
            logger.logWarning(LOGFROM, "no properties file loaded. using internal defaults where available");
            return defaultValue;
        }
        return properties.getProperty(p, defaultValue);
    }

    @Deprecated
    public static String getConfigProp(String p) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/v1.config.properties"));

            String value = prop.getProperty(p);
            switch (p) {
                case "connectionstring": {
                    value = enforceNewDBCredentials(value);
                    break;
                }
            }

            return value;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String enforceNewDBCredentials(String value) {
        int startUserName = value.indexOf("user=");
        int startPassword = value.indexOf("password=");
        int endPassword = value.indexOf('&', startPassword);
        return value.substring(0, startUserName) + "user=hutoma_caller&password=>YR\"khuN*.gF)V4#" + value.substring(endPassword);
    }
}
