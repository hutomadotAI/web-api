package hutoma.api.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by Hutoma on 07/07/16.
 */
public class userAIDomains {

    public static boolean insert_domain(
            String id,
            String aiid,
            Integer domid,
            boolean active
    ) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            String query = " insert into userAIDomains (id, aiid, domid, active)" + " values (?, ?, ?, ?)";

            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, id);
            preparedStmt.setString(2, aiid);
            preparedStmt.setInt(3, domid);
            preparedStmt.setBoolean(4, active);

            preparedStmt.execute();
            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
}
