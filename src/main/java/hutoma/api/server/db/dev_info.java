package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class dev_info {


    public static int get_dev_plan_training_time(String dev_id) {
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
            }

            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return maxt;
    }
}
