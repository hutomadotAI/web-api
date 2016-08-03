package com.hutoma.api.common;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by David MG on 02/08/2016.
 */
public class Config {

    public String getEncodingKey() {
        return getConfigProp("encoding_key");
    }

    public String getConfigProp(String p) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));
            return prop.getProperty(p);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
