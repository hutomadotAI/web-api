package hutoma.api.server.info.nodes;

import hutoma.api.server.utils.utils;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by mauriziocibelli on 19/04/16.
 */
public class wnet {



    public static String getAnswer(String devid, String aiid,String q,float min_p,boolean fastsearch) {
        try
        {

            String wnet_server = utils.getConfigProp("wnet_server");
            wnet_server += "&q="+q.replace(" ","%20");
            wnet_server += "&aiid="+aiid;
            wnet_server += "&uid="+devid;
            wnet_server += "&min_p="+min_p;
            if (fastsearch) wnet_server +="&fs=yes";
            URL url = new URL(wnet_server);
            return IOUtils.toString(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {}
        return "";
    }

}
