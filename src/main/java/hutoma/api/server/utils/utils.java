package hutoma.api.server.utils;

import java.io.*;

/**
 * Created by mauriziocibelli on 25/04/16.
 */
public class utils {

    public static String getConfigProp(String p) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + "/ai/config.properties"));
            return prop.getProperty(p);

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