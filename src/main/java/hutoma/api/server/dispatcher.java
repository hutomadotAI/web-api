package hutoma.api.server;

import hutoma.api.server.info.nodes.wnet;

/**
 * Created by mauriziocibelli on 25/04/16.
 */
public class dispatcher {

    public static String getAnswer(String devid,String aiid,String q,float min_p, boolean fastsearch) {
        return wnet.getAnswer(devid, aiid, q, min_p, fastsearch);
    }

}

