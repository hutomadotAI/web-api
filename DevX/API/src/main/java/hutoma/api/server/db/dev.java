package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;

import static hutoma.api.server.utils.utils.debug;
import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class dev {


    public static int get_dev_plan_training_time(String dev_id) throws SQLException, ClassNotFoundException {
        api_root._ai r = new api_root._ai();
        String pid="";
        int maxt = 0;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT plan_id FROM users WHERE dev_id='"+dev_id+"'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                pid = rs.getString("plan_id");
            }

            query = "SELECT maxtraining FROM devplan WHERE plan_id='"+pid+"'";
            rs = st.executeQuery(query);

            while (rs.next()) {
                maxt = rs.getInt("maxtraining");
                debug("DEV PLAN MAX:"+maxt);
            }

            st.close();
            conn.close();
        }

        catch (Exception e) {
            debug("DEV PLAN SELECTION EXCEPTION:"+e.getMessage());
        }
        return maxt;
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

}
