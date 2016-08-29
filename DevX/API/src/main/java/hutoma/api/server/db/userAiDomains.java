package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by Hutoma on 07/07/16.
 */
public class userAiDomains {

    public static boolean create_userAIDomain(
            String dev_token,
            String aiid,
            String dom_id,
            boolean active
    ) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            //TODO: move to stored procedure when it is being used somewhere
            String query = " INSERT INTO userAIDomains (dev_token, aiid, dom_id, active) VALUES (?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, dev_token);
            preparedStmt.setString(2, aiid);
            preparedStmt.setString(3, dom_id);
            preparedStmt.setBoolean(4, active);
            preparedStmt.execute();
            conn.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean update_userAIDomain(String dev_token, String aiid, String dom_id, Boolean active) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            //TODO: move to stored procedure when it is being used somewhere
            String query = " UPDATE userAIDomains SET active=? WHERE dev_token=? AND aiid=? AND dom_id=?";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, active);
            preparedStmt.setString(2, dev_token);
            preparedStmt.setString(3, aiid);
            preparedStmt.setString(4, dom_id);

            preparedStmt.execute();
            conn.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<api_root._userAIDomain> get_all_userAIDomains(String dev_token, String aiid) {
        ArrayList<api_root._userAIDomain> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            //TODO: move to stored procedure when it is being used somewhere
            String query = "SELECT * FROM userAIDomains WHERE dev_token='" + dev_token + "' AND aiid='" + aiid + "'";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                api_root._userAIDomain r = new api_root._userAIDomain();
                r.dev_token = rs.getString("dev_token");
                r.aiid = rs.getString("aiid");
                r.dom_id = rs.getString("dom_id");
                r.active = rs.getBoolean("active");
                r.created_on = rs.getDate("created_on");
                res.add(r);
            }
            st.close();
            conn.close();
            return res;

        } catch (Exception e) {
        }
        return res;
    }


    public static api_root._userAIDomain get_single_userAIDomain(String dev_token, String aiid, String dom_id) {
        api_root._userAIDomain r = new api_root._userAIDomain();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();

            //TODO: move to stored procedure when it is being used somewhere
            String query = "SELECT * FROM userAIDomains WHERE dev_token='" + dev_token + "' AND aiid='" + aiid + "' dom_id='" + dom_id + "'";

            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                r.dev_token = rs.getString("dev_token");
                r.aiid = rs.getString("aiid");
                r.dom_id = rs.getString("dom_id");
                r.active = rs.getBoolean("active");
                r.created_on = rs.getDate("created_on");
            }
            st.close();
            conn.close();
        } catch (Exception e) {
        }
        return r;
    }


    public static boolean delete_userAIDomain(String dev_token, String aiid, String dom_id) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            //TODO: move to stored procedure when it is being used somewhere
            String query = " DELETE FROM userAIDomains WHERE dev_token=? AND aiid=? AND dom_id=?";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, dev_token);
            preparedStmt.setString(2, aiid);
            preparedStmt.setString(3, dom_id);
            preparedStmt.execute();
            conn.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}