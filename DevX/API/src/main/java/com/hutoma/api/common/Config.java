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
        return getConfigProp("encoding_key", "");
    }

    public String getCoreQueue() {
        return getConfigProp("core_queue", "");
    }

    public Regions getMessageQueueRegion() {
        return Regions.US_EAST_1;
    }

    public String getWNetServer() {
        return getConfigProp("wnet_server", "");
    }

    public long getNeuralNetworkTimeout() {
        return Long.valueOf(getConfigProp("RNNTimeout", "60"));
    }

    public long getMaxUploadSize() {
        return Long.valueOf(getConfigProp("MaxUploadSize", "65536"));
    }

    public int getMaxClusterLines() {
        return Integer.valueOf(getConfigProp("max_cluster_lines", "10000"));
    }

    public double getClusterMinProbability() {
        return Double.valueOf(getConfigProp("cluster_min_probability", "0.7"));
    }

    public String getConfigProp(String p, String defaultValue) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));
            return prop.getProperty(p, defaultValue);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
