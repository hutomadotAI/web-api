package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by Andrea on 14/07/16.
 */
public class domain {

    public static boolean create_domain(
            String dom_id,
            String name,
            String description,
            String icon,
            String color,
            boolean available
    ) {
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);


            String query = " insert into domains (dom_id, name, description, icon, color, available) values (?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, dom_id);
            preparedStmt.setString(2, name);
            preparedStmt.setString(3, description);
            preparedStmt.setString(4, icon);
            preparedStmt.setString(5, color);
            preparedStmt.setBoolean(6, available);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean update_available_domain ( String dom_id, Boolean available) {
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " update domains set available=? where dom_id=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, available);
            preparedStmt.setString(2, dom_id);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<api_root._domain> get_all_domains() {
        ArrayList<api_root._domain> res = new ArrayList<>();
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM domains";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                api_root._domain r = new api_root._domain();
                r.dom_id = rs.getString("dom_id");
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.icon = rs.getString("icon");
                r.color = rs.getString("color");
                r.available = rs.getBoolean("available");
                res.add(r);
            }
            st.close();
            conn.close();
            return res;

        }

        catch (Exception e) {}
        return res;
    }


    public static api_root._domain get_domain(String dom_id) {
        api_root._domain r = new api_root._domain();
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();

            String query = "SELECT * FROM domains WHERE dom_id='"+dom_id+"'";

            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                r.dom_id = rs.getString("aiid");
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.icon = rs.getString("icon");
                r.color = rs.getString("color");
                r.available = rs.getBoolean("available");
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return r;
    }


    public static boolean delete_domain(String dom_id) {
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = " delete from domains where dom_id=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, dom_id);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

}
