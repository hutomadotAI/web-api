package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 01/05/16.
 */
public class query {

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

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " insert into ai (aiid, ai_name, ai_description,dev_id, is_private,deep_learning_error,deep_learning_status,shallow_learning_status,ai_status,client_token,ai_trainingfile)"
                                 + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
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
            return false;
        }
        return true;
    }


    public static boolean update_ai_training_file( String aiid, String training_file) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
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
        catch (Exception e) {
            return false;
        }
        return true;
    }


    public static boolean update_ai_training_status( String aiid, String status) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
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

            String myDriver = "org.gjt.mm.mysql.Driver";
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


    public static ArrayList<api_root._ai> get_all_ai(String dev_id) {
        ArrayList<api_root._ai> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM ai WHERE dev_id='"+dev_id+"'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                api_root._ai r = new api_root._ai();
                r.ai_status = rs.getInt("ai_status");
                r.aiid = rs.getString("aiid");
                r.created_on = rs.getDate("created_on");
                r.description = rs.getString("ai_description");
                r.name = rs.getString("ai_name");
                r.ai_training_file = rs.getString("ai_trainingfile");
                r.is_private = rs.getBoolean("is_private");
                r.deep_learning_error = rs.getDouble("deep_learning_error");
                r.training_status = rs.getString("deep_learning_status");
                r.client_token = rs.getString("client_token");
                res.add(r);
            }
            st.close();
            conn.close();
            return res;

        }

        catch (Exception e) {}
        return res;
    }


    public static api_root._ai get_ai(String aiid) {
        api_root._ai r = new api_root._ai();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM ai WHERE aiid='"+aiid+"'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                r.ai_status = rs.getInt("ai_status");
                r.aiid = rs.getString("aiid");
                r.created_on = rs.getDate("created_on");
                r.description = rs.getString("ai_description");
                r.name = rs.getString("ai_name");
                r.ai_training_file = rs.getString("ai_trainingfile");
                r.is_private = rs.getBoolean("is_private");
                r.deep_learning_error = rs.getDouble("deep_learning_error");
                r.training_status = rs.getString("deep_learning_status");
                r.client_token = rs.getString("client_token");

            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return r;
    }


    public static String get_ai_status(String aiid) {
        api_root._ai r = new api_root._ai();
        String stat="";
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
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

    public static boolean delete_dev(String dev_id) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = " delete from users where dev_id=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, dev_id);
            preparedStmt.execute();

            query = " delete from ai where dev_id=?";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, dev_id);
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }


    public static boolean clean_test_data() {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = " delete from users where dev_id like '%HUTOMA_TEST%'";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            query = " delete from ai where dev_id like '%HUTOMA_TEST%'";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean rnnQueueUpdate (String appid,String botid, int status) {
        boolean res=false;
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "UPDATE rnnQueue SET status ="+status+" WHERE appid='"+appid+"' AND botid='"+botid+"'";
            System.out.println("query:" + query);
            st.executeUpdate(query);
            st.close();
            conn.close();
            res =true;
        }
        catch (Exception e) {System.err.println("RNN QUEUE UPDATE:"+e.getMessage());}
        System.out.println("query res:"+res);

        return res;
    }

}
