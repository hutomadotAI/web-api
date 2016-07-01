package hutoma.api.server.db;

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
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            String query = " insert into chatlog (dev_id,message_from, message_to, question)"
                    + " values (?,?,?,?)";
            PreparedStatement preparedStmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString (1, dev_id);
            preparedStmt.setString (2, uid);
            preparedStmt.setString(3, aiid);
            preparedStmt.setString(4, q);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            preparedStmt.execute();
            ResultSet generatedKeys = preparedStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                rowid = generatedKeys.getLong(1);
            }
            conn.close();
        }
        catch (Exception e) {
            utils.debug("query exception:" + e.getMessage());

        }
        return rowid;
    }


    public static String getAnswer (long qid) {
        api_root._ai r = new api_root._ai();
        String answer="";
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT answer FROM chatlog WHERE id="+qid;
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                answer = rs.getString("NNActive");
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return answer;
    }


    public static boolean is_RNN_active(String dev_id,String aiid) {
        api_root._ai r = new api_root._ai();
        int stat=0;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT NNActive FROM ai WHERE dev_id='"+dev_id+"' AND ='"+aiid+"'";
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                stat = rs.getInt("NNActive");
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return stat>0;
    }


    public static boolean RNN_models_avaiable(String dev_id, String aiid) {
        api_root._ai r = new api_root._ai();
        boolean res =false;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
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
            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
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
