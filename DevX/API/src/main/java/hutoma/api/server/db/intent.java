package hutoma.api.server.db;

import hutoma.api.server.ai.api_intents_and_entities;
import java.sql.*;
import java.util.ArrayList;
import static com.hutoma.api.common.Config.getConfigProp;

/**
 * Created by mauriziocibelli on 28/07/16.
 */
public class intent {

    /**
     *  Get a list of all intents created by the developer
     * @param dev_id the developer ID
     * @return an array of class type intent
     */
    public static ArrayList<api_intents_and_entities._intent> get_dev_intents(String dev_id) {
        ArrayList<api_intents_and_entities._intent> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM intents where dev_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            // process the results
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                api_intents_and_entities._intent r = new api_intents_and_entities._intent();
                r.intent_id = rs.getString("intent_id");
                r.intent_name = rs.getString("intent_name");
                r.topic_in = rs.getString("topic_in");
                r.topic_out = rs.getString("topic_out");
                r.response= rs.getString("response");
                r.aiid= rs.getString("aiid");
                String tmp = rs.getString("training_data");
                r.training_data = tmp.split("\\|\\|");
                tmp = rs.getString("entity_list");
                r.entity_list = tmp.split("\\|\\|");

                res.add(r);
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }

    /**
     * returns the list of intents associated to a specific ai
     *
     * @param dev_id the developer id
     * @param aiid the ai id
     * @return a class intent
     */

    public static ArrayList<api_intents_and_entities._intent> get_ai_intents(String dev_id, String aiid) {
        ArrayList<api_intents_and_entities._intent> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM intents where dev_id=? and aiid=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, aiid);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                api_intents_and_entities._intent r = new api_intents_and_entities._intent();
                r.intent_id = rs.getString("intent_id");
                r.intent_name = rs.getString("intent_name");
                r.topic_in = rs.getString("topic_in");
                r.topic_out = rs.getString("topic_out");
                r.response= rs.getString("response");
                r.aiid= rs.getString("aiid");
                String tmp = rs.getString("training_data");
                r.training_data = tmp.split("\\|\\|");
                tmp = rs.getString("entity_list");
                r.entity_list = tmp.split("\\|\\|");
                res.add(r);
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }


    /**
     * Returns details about a specific intent
     *
     * @param dev_id the dev id
     * @param intent_id the intent id
     * @return intent
     */
    public static api_intents_and_entities._intent get_intent(String dev_id, String intent_name) {
        api_intents_and_entities._intent res = new  api_intents_and_entities._intent();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM intents where dev_id=? and intent_name=? ";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, intent_name);
            // process the results
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                res.intent_id = rs.getString("intent_id");
                res.intent_name = rs.getString("intent_name");
                res.topic_in = rs.getString("topic_in");
                res.topic_out = rs.getString("topic_out");
                res.response= rs.getString("response");
                res.aiid= rs.getString("aiid");
                String tmp = rs.getString("training_data");
                res.training_data = tmp.split("\\|\\|");
                tmp = rs.getString("entity_list");
                res.entity_list = tmp.split("\\|\\|");

            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }


    /**
     *  Creates a new intent for the specific AI
     *
     * @param dev_id the developer id
     * @param intent_name the intent name
     * @param aiid the ai id
     * @param topic_in the topic that should be present to trigger this inent
     * @param topic_out the topic that this intent will set once triggered
     * @param training_data the list of sample phrases that defines this intent
     * @param response the output once the intent is triggered
     * @return true if the intent is created. false otherwise
     */
    public static boolean create_intent (String dev_id,
                                         String intent_name,
                                         String entity_list,
                                         String aiid,
                                         String topic_in,
                                         String topic_out,
                                         String training_data,
                                         String response
                                         ) {
        long nrows=0;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "INSERT INTO intents (dev_id,intent_id,intent_name,aiid,topic_in,topic_out,training_data,response,entity_list) " +
                            "VALUES (?,?,?,?,?,?,?,?,?) ";

            String intent_id = java.util.UUID.randomUUID().toString();
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, intent_id);
            ps.setString(3, intent_name);
            ps.setString(4, aiid);
            ps.setString(5, topic_in);
            ps.setString(6, topic_out);
            ps.setString(7, training_data);
            ps.setString(8, response);
            ps.setString(9, entity_list);

            // process the results
            nrows = ps.executeLargeUpdate();


            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return nrows>0;
    }


    /**
     * Deletes an intent
     *
     * @param dev_id the dev id
     * @param intent_name the intent id
     */
    public static boolean delete_intent (String dev_id, String intent_name) {
        long nrows=0;

        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "DELETE FROM intents WHERE  dev_id=? and intent_name=? ";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, intent_name);
            // process the results
            nrows = ps.executeLargeUpdate();

            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return nrows>0;
    }

    /**
     *  Deletes all dev intents
     * @param dev_id the dev id
     * @return true if sucesfull. False otherwise
     */
    public static boolean delete_all_intents  (String dev_id)
    {
        long nrows=0;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "DELETE FROM intents WHERE  dev_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            nrows = ps.executeLargeUpdate();

            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return nrows>0;
    }

}
