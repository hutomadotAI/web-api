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

    public static String debug(String txt) throws ClassNotFoundException, SQLException {

        try {
            String debug = getConfigProp("debug");

            if (debug.equals("true")) {

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                txt = (dateFormat.format(date))+" "+txt.replace("'","\\'");
                String myDriver = "org.gjt.mm.mysql.Driver";
                String myUrl = getConfigProp("connectionstring");
                Class.forName(myDriver);
                Connection conn = DriverManager.getConnection(myUrl);
                Statement st = conn.createStatement();
                String query = "INSERT INTO debug (text) VALUES ('" + txt + "')";
                st.executeUpdate(query);
                st.close();
                conn.close();
            }
        }
        catch (Exception e) {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "INSERT INTO debug (text) VALUES ('" + e.getMessage() + "')";
            st.executeUpdate(query);
            st.close();
            conn.close();
            return "exception:" + e.getMessage();
        }
        return "saved";

    }
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