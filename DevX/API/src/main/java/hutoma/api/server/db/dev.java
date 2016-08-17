package hutoma.api.server.db;

import hutoma.api.server.ai.api_root;

import java.sql.*;

import static hutoma.api.server.utils.utils.debug;
import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class dev {

    public static boolean delete_dev(String dev_id) {
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
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
