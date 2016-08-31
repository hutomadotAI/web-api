package com.hutoma.api.common;

import com.amazonaws.regions.Regions;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by David MG on 02/08/2016.
 */
@Singleton
public class Config {

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

    public double getClusterMinProbability() {
        return Double.valueOf(getConfigFromProperties("cluster_min_probability", "0.7"));
    }

    private String getConfigFromProperties(String p, String defaultValue) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));
            return prop.getProperty(p, defaultValue);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Deprecated
    public static String getConfigProp(String p) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));

            String value = prop.getProperty(p);
            switch (p) {
                case "connectionstring": {
                    //replace username and password for DB login here
                    int startUserName = value.indexOf("user=");
                    int startPassword = value.indexOf("password=");
                    int endPassword = value.indexOf('&', startPassword);
                    String newConnectionString = value.substring(0, startUserName) + "user=hutoma_caller&password=>YR\"khuN*.gF)V4#" + value.substring(endPassword);
                    value = newConnectionString;
                    break;
                }
            }

            return value;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
