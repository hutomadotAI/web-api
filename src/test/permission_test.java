import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.ai.api_root;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by mauriziocibelli on 29/04/16.
 */
public class permission_test extends base_test{

    @Test
    public void create_dev_with_no_permission() throws IOException {
        try {
            super.curl(super._test_client_token, "POST", _curl_ADMIN_PUT_CREATE_DEV.replace("__ROLE__", "ROLE_ADMIN").replace("__DEVID__", super._test_devid));
            assert (false);
        }
        catch(Exception ex) {assert (true);}
    }

    @Test
    public void delete_dev_with_no_permission() throws IOException {
        try {
            super.curl(super._test_client_token, "DELETE", _curl_ADMIN_PUT_CREATE_DEV.replace("__DEVID__", super._test_devid));
            assert (false);
        }
        catch(Exception ex) {assert (true);}
    }

    @Test
    public void delete_someone_ai() throws IOException {
        try {
            String response = super.curl(super._test_client_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", "costa"));
            assert (false);
        }
        catch(Exception ex) {assert (true);}
    }

    @Test
    public void create_and_delete_its_own_ai() throws IOException {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //creates a new AI
            String json = super.curl(super._test_dev_token, "POST", _curl_POST_CREATE_AI);
            api_root._ai ai = new api_root._ai();
            ai = gson.fromJson(json,api_root._ai.class);
            assert(!ai.aiid.isEmpty());
            String newai =ai.aiid;

            //creates a second  AI
            super.curl(super._test_dev_token, "POST", _curl_POST_CREATE_AI);


            //gets all created AIs
            json = super.curl(super._test_dev_token, "GET", _curl_GET_GET_ALLAIs);
            api_root._myAIs _ai = gson.fromJson(json,api_root._myAIs.class);
            assert(_ai.ai_list.size()==3);
            assert(_ai.ai_list.get(1).aiid.equals(newai));
            String newai2 =_ai.ai_list.get(2).aiid;

            //get single AI
            json= super.curl(super._test_dev_token, "GET", _curl_GET_GET_SINGLEAI.replace("__AIID__", newai));
            _ai = gson.fromJson(json, api_root._myAIs.class);
            assert(_ai.ai_list.get(0).aiid.equals(newai));


            //now deletes the AI created
            super.curl(super._test_dev_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", newai));
            super.curl(super._test_dev_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", newai2));

            json = super.curl(super._test_dev_token, "GET", _curl_GET_GET_ALLAIs);
            _ai = gson.fromJson(json, api_root._myAIs.class);
            assert(_ai.ai_list.size()==1);

        }
        catch(Exception ex) {assert (false);}
    }

}
