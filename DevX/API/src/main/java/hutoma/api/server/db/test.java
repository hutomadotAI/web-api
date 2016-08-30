package hutoma.api.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import static com.hutoma.api.common.Config.getConfigProp;

/**
 * Created by mauriziocibelli on 22/06/16.
 */
public class test {


    public static boolean clean_test_data(String dev_id,String aiid) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);

            //TODO: move to stored procedure when it is being used somewhere
            String query = " delete from users where dev_id like '%HUTOMA_TEST%'";
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            query = " delete from ai where dev_id like '%HUTOMA_TEST%'";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            query = " delete * from ai_memory where aiid='"+aiid+"'";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            query = " delete * from intents where dev_id='"+dev_id+"'";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();

            query = " delete * from entities where aiid='"+aiid+"'";
            preparedStmt = conn.prepareStatement(query);
            preparedStmt.execute();


            conn.close();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
}
