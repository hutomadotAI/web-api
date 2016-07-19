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
        public static class _debug {
            public double score;
            public long elapsted_time;
            public String type;
            public String answer;
        }
        public static class _metadata {
            public String emotion;
            public String topic;
        }

    public static class _parameter {
        public String name;
        public String value;
    }


    public static class _result {
            public double score;
            public String query = "";
           // public String resolvedQuery = "";
            public String answer = "";
            public float elapsed_time;
           // public String source;
            public String action;
            public _parameter[] parameters;
            public String context;
          //  public _metadata metadata;
            public ArrayList<_debug> debug_info;
        }
        public static class _chat {
            public String id;
            public  String timestamp;
            public _result result;
            public _metadata metadata;
            public _status status;

        }

       public static class _newai {
           public _status status;
           public String aiid;
           public String client_token;
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

    public static class _myAIs {

        public _status status;
        public String devid;
        public String dev_token;
        public ArrayList<_ai> ai_list;
        public _ai ai;
    }

    public static class _domain {

        public String dom_id;
        public String name;
        public String description;
        public String icon;
        public String color;
        public boolean available;
    }

    public static class _domainList {

        public _status status;
        public ArrayList<_domain> domain_list;
        public _domain domain;
    }

    public static class _integration {

        public String id;
        public String name;
        public String description;
        public String icon;
        public boolean available;
    }


}
