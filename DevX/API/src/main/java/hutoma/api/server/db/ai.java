package hutoma.api.server.db;

import com.hutoma.api.containers.ApiAi;
import hutoma.api.server.ai.api_root;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 01/05/16.
 */
public class ai {

    public static boolean create_ai(
                              String aiid,
                              String name,
                              String description,
                              String dev_id,
                              boolean is_private,
                              double deep_learning_error,
                              int deep_learning_status,
                              int shallow_learning_status,
                              int status,
                              String client_token,
                              String training_file
                              ) {
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = "CALL addAI(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, aiid);
            preparedStmt.setString(2, name);
            preparedStmt.setString(3, description);
            preparedStmt.setString(4, dev_id);
            preparedStmt.setBoolean(5, is_private);
            preparedStmt.setDouble(6, deep_learning_error);
            preparedStmt.setInt(7, deep_learning_status);
            preparedStmt.setInt(8, shallow_learning_status);
            preparedStmt.setInt(9, status);
            preparedStmt.setString(10, client_token);
            preparedStmt.setString(11, training_file);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean create_dev(
            String username,
            String email,
            String password,
            String password_salt,
            String name,
            String attempt,
            String dev_token,
            int plan_id,
            String dev_id
            )
    {
        Boolean result = true;

        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " insert into users (username, email, password,password_salt,name,created,attempt,dev_token,plan_id,dev_id)"
                         + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, username);
            preparedStmt.setString(2, email);
            preparedStmt.setString(3, password);
            preparedStmt.setString(4, password_salt);
            preparedStmt.setString(5, name);

            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            preparedStmt.setDate(6, sqlDate);
            preparedStmt.setString(7, attempt);
            preparedStmt.setString(8, dev_token);
            preparedStmt.setInt(9, plan_id);
            preparedStmt.setString(10, dev_id);
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e) {
            result = false;
        }

        return result;
    }


    public static void update_ai_training_file( String aiid, String training_file) throws Exception {
        String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);

        String query = " update ai set ai_trainingfile=? where aiid=?";
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, training_file);
        preparedStmt.setString(2, aiid);
        preparedStmt.execute();
        conn.close();
    }


    public static boolean update_ai_training_status( String aiid, String status) {
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " update ai set ai_status=? where aiid=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, status);
            preparedStmt.setString(2, aiid);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }



    public static boolean delete_ai(String aiid) {
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = " delete from ai where aiid=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, aiid);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<ApiAi> get_all_ai(String dev_id) {
        ArrayList<ApiAi> res = new ArrayList<>();
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            Statement st = conn.createStatement();
            String query = "SELECT * FROM ai WHERE dev_id='"+dev_id+"'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                ApiAi ai = new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                        new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                        null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
                res.add(ai);
            }
            st.close();
            conn.close();
        }
        catch (Exception e) {
            return null;
        }
        return res;
    }

    public static ApiAi get_ai(String aiid) {

        ApiAi ai = null;
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM ai WHERE aiid='"+aiid+"'";
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                ai = new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                        new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                        null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
            }
            st.close();
            conn.close();
        }
        catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return ai;
    }


    public static String get_ai_status(String aiid) {
        api_root._ai r = new api_root._ai();
        String stat="";
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT ai_status FROM ai WHERE aiid='"+aiid+"'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                stat = rs.getString("ai_status");
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return stat;
    }





}
