package hutoma.api.server.db;

import com.hutoma.api.common.Logger;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.utils.utils;

import java.sql.*;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 22/06/16.
 */
public class RNN {

    public static long insertQuestion (String dev_id, String uid, String aiid, String q) throws SQLException, ClassNotFoundException {
        long rowid = -1;
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = "CALL insertQuestion(?,?,?,?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString (1, dev_id);
            preparedStmt.setString (2, uid);
            preparedStmt.setString(3, aiid);
            preparedStmt.setString(4, q);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            ResultSet generatedKeys = preparedStmt.executeQuery();
            if (generatedKeys.next()) {
                rowid = generatedKeys.getLong(1);
            }
            preparedStmt.close();
            conn.close();
        }
        catch (Exception e) {
            return -1;
        }
        return rowid;
    }


    public static String getAnswer(long qid) throws Exception {
        api_root._ai r = new api_root._ai();
        String answer="";
        String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);

        String query = "CALL getAnswer(?)";
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setLong (1, qid);

        ResultSet rs = preparedStmt.executeQuery();

        // TODO: fix and figure out why we get more than one result here and each one overwrites the last one
        while (rs.next()) {
            answer = rs.getString("answer");
        }

        preparedStmt.close();
        conn.close();
        return answer;
    }

    public static boolean is_RNN_active(String dev_id, String aiid) throws Exception {
        api_root._ai r = new api_root._ai();
        int stat=0;
        boolean result  =false;

        String myDriver = "com.mysql.cj.jdbc.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "CALL getAiActive(?,?)";

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString (1, aiid);
        preparedStmt.setString (2, dev_id);

        ResultSet rs = preparedStmt.executeQuery();

        while (rs.next()) {
            stat = rs.getInt("NNActive");
            if (stat == 1 )  result = true;
        }
        preparedStmt.close();
        conn.close();

        return result;
    }


    public static boolean RNN_models_avaiable(String dev_id, String aiid) {
        api_root._ai r = new api_root._ai();
        boolean res =false;
        try {

            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();

            //TODO: move to stored procedure when it is being used somewhere
            String query = "SELECT model_files_available FROM ai WHERE dev_id='"+dev_id+"' AND aiid='"+aiid+"'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                int stat = rs.getInt("ai_status");
                res = stat > 0;
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }



    public static boolean rnnQueueUpdate (String appid,String botid, int status) {
        boolean res=false;
        try {
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();

            //TODO: move to stored procedure when it is being used somewhere
            String query = "UPDATE rnnQueue SET status ="+status+" WHERE appid='"+appid+"' AND botid='"+botid+"'";
            System.out.println("ai:" + query);
            st.executeUpdate(query);
            st.close();
            conn.close();
            res =true;
        }
        catch (Exception e) {System.err.println("RNN QUEUE UPDATE:"+e.getMessage());}
        System.out.println("ai res:"+res);

        return res;
    }

}
