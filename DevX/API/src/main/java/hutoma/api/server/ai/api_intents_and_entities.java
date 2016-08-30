package hutoma.api.server.ai;

import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 28/07/16.
 */

// A basic class holding the objects that are mapped to the DB
public class api_intents_and_entities {


    public static class _intent {
        public String intent_id;
        public String intent_name;
        public String topic_in;
        public String topic_out;
        public String response;
        public String aiid;
        public String[] training_data;
        public String[] entity_list;
    }

    public static class memory_token {
        public String variable_name;
        public String variable_value;
        public String variable_type;
        public java.sql.Timestamp last_accessed;
        public int expires_seconds;
        public int n_prompts;
    }

    public static class REST_RESPONSE_ai_memory {
        public ArrayList<memory_token> memory;
        public api_root._status status;
    }


}