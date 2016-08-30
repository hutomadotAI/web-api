package hutoma.api.server.db;

import com.hutoma.api.containers.ApiMemoryToken;
import hutoma.api.server.ai.api_intents_and_entities;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.hutoma.api.common.Config.getConfigProp;

/**
 * Created by mauriziocibelli on 28/07/16.
 */
public class memory {


    /**
     *  Returns the number of seconds the last time a memory variable was accessed. We used this to purge a variable from the AI memory after
     *  a number of seconds X defined by the developer
     *
     * @param currentTime
     * @param oldTime
     * @return
     */
    private static long diff(java.sql.Timestamp currentTime, java.sql.Timestamp oldTime)
    {
        long milliseconds1 = oldTime.getTime();
        long milliseconds2 = currentTime.getTime();
        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return diffSeconds;
    }

    public static void purge_memory (String dev_id, String aiid, String uid) {

        try {

            java.util.Date date= new java.util.Date();
            Timestamp timestamp = new Timestamp(date.getTime());


            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM ai_memory where aiid=? and uid=? and dev_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, aiid);
            ps.setString(2, uid);
            ps.setString(3, dev_id);
            // process the results
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                api_intents_and_entities.memory_token r = new api_intents_and_entities.memory_token();
                r.variable_name = rs.getString("variable_name");
                r.last_accessed = rs.getTimestamp("last_accessed");
                r.expires_seconds= rs.getInt("expires_seconds");
                long secs = diff(timestamp, r.last_accessed );
                if (secs>r.expires_seconds) remove_variable(dev_id,aiid,uid,r.variable_name);

            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
    }




    /**
     * This returns the list of all variables that are stored in the temp AI memory
     *
     * @param aiid the AI id
     * @param uid the user id that is interacting with the AI
     * @return a list of variables that have been set in memory for the specific AI and the specific end user
     */
    public static List<ApiMemoryToken> get_all_user_variables(String dev_id, String aiid, String uid) throws ClassNotFoundException, SQLException {
        List<ApiMemoryToken> res = new ArrayList<>();
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "SELECT * FROM ai_memory where aiid=? and uid=? and dev_id=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, uid);
        ps.setString(3, dev_id);
        // process the results
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            ApiMemoryToken token = new ApiMemoryToken(rs.getString("variable_name"), rs.getString("variable_value"),
                    rs.getString("variable_type"),
                    new DateTime(rs.getTimestamp("last_accessed")),
                    rs.getInt("expires_seconds"), rs.getInt("n_prompts"));
            res.add(token);
        }
        st.close();
        conn.close();
        return res;
    }


    /**
     *  Returns the pair key-value for a specific variable
     * @param aiid The AI id
     * @param uid The end user ID
     * @param variable_name the specific variable name to retrieve
     * @return
     */
    public static ApiMemoryToken get_user_variable(String dev_id, String aiid, String uid, String variable_name) throws ClassNotFoundException, SQLException {

        ApiMemoryToken token = null;
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "SELECT * FROM ai_memory where aiid=? and uid=? and variable_name=? and dev_id=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, uid);
        ps.setString(3,variable_name);
        ps.setString(4, dev_id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            token = new ApiMemoryToken(rs.getString("variable_name"), rs.getString("variable_value"),
                    rs.getString("variable_type"),
                    new DateTime(rs.getTimestamp("last_accessed")),
                    rs.getInt("expires_seconds"), rs.getInt("n_prompts"));
        }
        st.close();
        conn.close();

        return token;
    }


    /**
     *
     * Add a new variable in the AI memory associated to the specific user.
     *
     * @param aiid the AI id
     * @param uid  the end user ID
     * @param expires_seconds the number of seconds after which the variable should be removed from memory
     * @param n_prompt the number of times that the AI will prompt the user about this variable if the variable is marked mandatory
     * @param variabel_type the variable category (ex. Family Memeber, Item, etc).
     * @param variable_name the name of variable
     * @param variable_value the value of the variable
     * @return
     */
    public static boolean set_variable (String dev_id,
                                        String aiid,
                                        String uid,
                                        int expires_seconds,
                                        int n_prompt,
                                        String variabel_type,
                                        String variable_name,
                                        String variable_value) throws ClassNotFoundException, SQLException {
        long nrows=0;
        java.util.Date date= new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "INSERT INTO ai_memory (aiid,uid,variable_name,variable_value,last_accessed,expires_seconds,n_prompts,variable_type,dev_id) " +
                       "VALUES (?,?,?,?,?,?,?,?,?) " +
                       "ON DUPLICATE KEY " +
                       "UPDATE variable_value=VALUES(variable_value), last_accessed=VALUES(last_accessed),n_prompts=VALUES(n_prompts)";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, uid);
        ps.setString(3, variable_name);
        ps.setString(4, variable_value);
        ps.setTimestamp(5, timestamp);
        ps.setInt(6,expires_seconds);
        ps.setInt(7,n_prompt);
        ps.setString(8,variabel_type);
        ps.setString(9,dev_id);
        nrows= ps.executeUpdate();
        st.close();
        conn.close();
        return nrows>0;
    }


    /**
     * Removes a variable from memory
     *
     * @param aiid the AI id
     * @param uid the user id
     * @param variable_name the name of the variable to remove
     */
    public static boolean remove_variable (String dev_id, String aiid, String uid, String variable_name) throws SQLException, ClassNotFoundException {

        int nRows = 0;
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "DELETE FROM ai_memory WHERE  aiid=? and uid=? and variable_name = ? and dev_id=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, uid);
        ps.setString(3, variable_name);
        ps.setString(4, dev_id);
        nRows = ps.executeUpdate();
        st.close();
        conn.close();
        return nRows>0;
    }


    /**
     *  Erase the AI memory for the specific end user
     * @param aiid the AI id
     * @param uid the user id
     */
    public static boolean remove_all_user_variables (String dev_id, String aiid, String uid) throws ClassNotFoundException, SQLException {
        int nRows = 0;
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "DELETE FROM ai_memory WHERE  aiid=? and uid=? and dev_id=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, uid);
        ps.setString(3, dev_id);
        nRows = ps.executeUpdate();
        st.close();
        conn.close();
        return nRows>0;
    }


    /**
     * Erase the AI memory for all users
     * @param aiid the AI id
     */
    public static boolean remove_all_ai_variables (String dev_id, String aiid) throws SQLException, ClassNotFoundException {

        int nRows = 0;
        String myDriver = "org.gjt.mm.mysql.Driver";
        String myUrl = getConfigProp("connectionstring");
        Class.forName(myDriver);
        Connection conn = DriverManager.getConnection(myUrl);
        Statement st = conn.createStatement();
        String query = "DELETE FROM ai_memory WHERE  aiid=? and dev_id=?) ";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, aiid);
        ps.setString(2, dev_id);
        nRows = ps.executeUpdate();
        st.close();
        conn.close();
        return nRows>0;
    }
}
