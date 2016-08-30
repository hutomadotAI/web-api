package hutoma.api.server.ai;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 25/04/16.
 */

public class api_root {

    public static class _status {

        public int code = 200;
        public String info = "";
        public String id;

    }

    public static class _ai {

        public String aiid;
        public String name;
        public String description;
        public Date created_on;
        public boolean is_private;
        public double deep_learning_error;
        public String training_debug_info;
        public String training_status;
        public String ai_status;
        public String client_token;
        public String ai_training_file;
    }

    public static class _domain {

        public String dom_id;
        public String name;
        public String description;
        public String icon;
        public String color;
        public boolean available;
    }

    public static class _integration {

        public String id;
        public String name;
        public String description;
        public String icon;
        public boolean available;
    }

}
