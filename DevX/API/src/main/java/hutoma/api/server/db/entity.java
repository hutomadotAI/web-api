package hutoma.api.server.db;

import hutoma.api.server.ai.api_intents_and_entities;

import java.sql.*;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 28/07/16.
 */
public class entity {


    /**
     * Retrieves the list of entities created by the dev
     *
     * @param dev_id the dev_id
     * @return a list of intents
     *
     */
    public static ArrayList<api_intents_and_entities._entity> get_dev_entities(String dev_id) {
        ArrayList<api_intents_and_entities._entity> res = new ArrayList<>();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM entities where dev_id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            // process the results
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                api_intents_and_entities._entity r = new api_intents_and_entities._entity();
                r.entity_id = rs.getString("entity_id");
                String tmp = rs.getString("entity_keys");
                r.entity_keys = tmp.split("\\|\\|");
                r.entity_name = rs.getString("entity_name");
                tmp = rs.getString("prompts");
                r.prompts = tmp.split("\\|\\|");
                r.max_prompts = rs.getInt("max_prompts");
                r.required = rs.getBoolean("required");

                res.add(r);
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }


    /**
     *  Retrievs a specific itent
     *
     * @param dev_id the dev id owning the intent
     * @param entity_name the entity name
     * @return an object containing the entity details
     */
    public static api_intents_and_entities._entity get_entity(String dev_id, String entity_name) {
        api_intents_and_entities._entity res = new api_intents_and_entities._entity();
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "SELECT * FROM entities where dev_id=? and entity_name=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, entity_name);
            // process the results
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                res.entity_id = rs.getString("entity_id");
                String tmp = rs.getString("entity_keys");
                res.entity_keys = tmp.split("\\|\\|");
                res.entity_name = rs.getString("entity_name");
                tmp = rs.getString("prompts");
                res.prompts = tmp.split("\\|\\|");
                res.max_prompts = rs.getInt("max_prompts");
                res.required = rs.getBoolean("required");
            }
            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return res;
    }


    /**
     *  Creates a new entity.
     *
     * @param dev_id the dev id owning the entity
     * @param entity_name the entity name
     * @param entity_keys the different values this entity could have
     * @param prompts what to say if this entity is not set and it is flagged as required
     * @param max_prompts how many times to prompt for this variable before we give up
     * @param required if a variable is required and has not been instantiated, the intent wont be triggered
     * @return
     */
    public static boolean create_entity (String dev_id,
                                         String entity_name,
                                         String entity_keys,
                                         String prompts,
                                         int max_prompts,
                                         boolean required
    ) {
        long nrows=0;
        try {

            String entity_id = java.util.UUID.randomUUID().toString();

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "INSERT INTO entities (entity_id,dev_id,entity_name,entity_keys,prompts,max_prompts,required) " +
                           "VALUES (?,?,?,?,?,?,?) ";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, entity_id);
            ps.setString(2, dev_id);
            ps.setString(3, entity_name);
            ps.setString(4, entity_keys);
            ps.setString(5, prompts);
            ps.setInt(6,max_prompts);
            ps.setBoolean(7,required);

            // process the results
            nrows = ps.executeUpdate();


            st.close();
            conn.close();
        }

        catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return nrows>0;
    }


    /**
     *  Updates and existing entity
     *
     * @param dev_id the dev id owning the entity
     * @param entity_name the entity to update
     * @param entity_keys the value to update
     * @param prompts
     * @param max_prompts
     * @param required
     * @return
     */
    public static boolean update_entity (String dev_id,
                                         String entity_name,
                                         String entity_keys,
                                         String prompts,
                                         int max_prompts,
                                         boolean required
    ) {
        long nrows=0;
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "UPDATE  entities SET entity_keys=?,prompts=?,max_prompts=?,required=? " +
                           "WHERE entity_name=? and dev_id=? ";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, entity_keys);
            ps.setString(2, prompts);
            ps.setInt(3,max_prompts);
            ps.setBoolean(4,required);
            ps.setString(5, entity_name);
            ps.setString(6, dev_id);

            // process the results
            nrows = ps.executeUpdate();


            st.close();
            conn.close();
        }

        catch (Exception e) {}
        return nrows>0;
    }


    /**
     *
     * deletes an entity
     *
     * @param dev_id the dev id
     * @param entity_name the entity name to delete
     */

    public static void delete_entity  (String dev_id, String entity_name) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "DELETE FROM entities WHERE dev_id=? and entity_name=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            ps.setString(2, entity_name);
            // process the results
            ps.execute();
            st.close();
            conn.close();
        }

        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Deletes all entities for a given dev
     *
     * @param dev_id the dev id
     */
    public static void delete_all_entities  (String dev_id) {
        try {

            String myDriver = "org.gjt.mm.mysql.Driver";
            String myUrl = getConfigProp("connectionstring");
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl);
            Statement st = conn.createStatement();
            String query = "DELETE FROM entities WHERE  dev_id=? ";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, dev_id);
            // process the results
            ps.execute();
            st.close();
            conn.close();
        }

        catch (Exception e) {}
    }

}
