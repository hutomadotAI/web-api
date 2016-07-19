package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by Hutoma on 14/07/16.
 */
public class integration {

    public static boolean create_integration(
            String id,
            String name,
            String description,
            String icon,
            boolean available
    ) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " insert into integrations (id, name, description, icon, available)"
                    + " values (?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, id);
            preparedStmt.setString(2, name);
            preparedStmt.setString(3, description);
            preparedStmt.setString(4, icon);
            preparedStmt.setBoolean(5, available);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean update_available_integration( String id, Boolean available) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " update integrations set available=? where id=?";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, available);
            preparedStmt.setString(2, id);
            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<api_root._integration> get_all_integrations() {
        ArrayList<api_root._integration> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM integrations";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                api_root._integration r = new api_root._integration();
                r.id = rs.getString("id");
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.icon = rs.getString("icon");
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


    public static api_root._integration get_integration(String id) {
        api_root._integration r = new api_root._integration();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM integrations WHERE id='"+id+"'";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                r.id = rs.getString("id");
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.icon = rs.getString("icon");
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

            String myDriver = "org.gjt.mm.mysql.Driver";
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
