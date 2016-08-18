package hutoma.api.server.utils;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mauriziocibelli on 25/04/16.
 */
public class utils {

    public static String getConfigProp(String p) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));



            String value = prop.getProperty(p);
            switch(p) {
                case "connectionstring": {
                    //replace username and password for DB login here
                    int startUserName = value.indexOf("user=");
                    int startPassword = value.indexOf("password=");
                    int endPassword = value.indexOf('&', startPassword);
                    String newConnectionString = value.substring(0,startUserName) +  "user=hutoma_caller&password=>YR\"khuN*.gF)V4#" + value.substring(endPassword);
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

    public static boolean writeToFile(InputStream uploadedInputStream,
                                   String uploadedFileLocation) {
        try {
            OutputStream out;
            int read;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(uploadedFileLocation,false);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) { return false;}
        return true;
    }

}