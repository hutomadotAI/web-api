package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by Hutoma on 07/07/16.
 */
public class userAIDomains {

    public static boolean create_userAIDomain(
            String dev_id,
            String aiid,
            String dom_id,
            boolean active
    ) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " INSERT INTO userAIDomains (dev_id, aiid, dom_id, active) VALUES (?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, dev_id);
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

    public static boolean update_userAIDomain(String dev_id, String aiid, String dom_id, Boolean active) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " UPDATE userAIDomains SET active=? WHERE dev_id=? AND aiid=? AND dom_id=?";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, active);
            preparedStmt.setString(2, dev_id);
            preparedStmt.setString(3, aiid);
            preparedStmt.setString(4, dom_id);

            preparedStmt.execute();
            conn.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<api_root._userAIDomain> get_all_userAIDomains(String dev_id, String aiid) {
        ArrayList<api_root._userAIDomain> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = "SELECT * FROM userAIDomains WHERE dev_id='" + dev_id + "' AND aiid='" + aiid + "'";

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                api_root._userAIDomain r = new api_root._userAIDomain();
                r.dev_id = rs.getString("dev_id");
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


    public static api_root._userAIDomain get_single_userAIDomain(String dev_id, String aiid, String dom_id) {
        api_root._userAIDomain r = new api_root._userAIDomain();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();

            String query = "SELECT * FROM userAIDomains WHERE dev_id='" + dev_id + "' AND aiid='" + aiid + "' dom_id='" + dom_id + "'";

            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                r.dev_id = rs.getString("dev_id");
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


    public static boolean delete_userAIDomain(String dev_id, String aiid, String dom_id) {
        try {
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " DELETE FROM userAIDomains WHERE dev_id=? AND aiid=? AND dom_id=?";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, dev_id);
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