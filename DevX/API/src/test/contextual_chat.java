import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.ai.api_root;
import org.junit.Test;

import java.io.IOException;

import static hutoma.api.server.db.ai.get_ai_status;

/**
 * Created by mauriziocibelli on 22/06/16.
 */
public class contextual_chat extends  base_test {

    @Test
    public void chat() throws IOException {
        try {


            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //creates a new AI
            String json = super.curl(super._test_dev_token, "POST", _curl_POST_CREATE_AI);
            api_root._ai ai = new api_root._ai();
            ai = gson.fromJson(json, api_root._ai.class);
            assert (!ai.aiid.isEmpty());


            // upload training file
            ProcessBuilder pb = new ProcessBuilder("curl", "-X","POST","-H","Authorization: Bearer "+_test_dev_token,_curl_PUT_UPLOAD_TRAINING.replace("__AIID__", ai.aiid),"-F","file=@"+System.getProperty("user.home") + "/ai/contextual.txt");
            System.out.print(pb.command().toString());
            Process p = pb.start();

            int counter = 0;
            String stat ="";
            while (counter < 120) {
                stat = get_ai_status(ai.aiid);
                if (stat.equals(String.valueOf(msg.training_in_progress))) break;
                Thread.sleep(1000);
                counter ++;
            }


            String question = "hey good morning to you. how is your day going?";
            json = super.curl(super._test_dev_token, "GET", _curl_GET_CHAT.replace("__AIID__",ai.aiid)+"?q="+question.replace(" ","%20")+"&min_p=0");


            question = "how is your day going?";
            json = super.curl(super._test_dev_token, "GET", _curl_GET_CHAT.replace("__AIID__",ai.aiid)+"?q="+question.replace(" ","%20")+"&uid=123&min_p=0");

            if (stat.isEmpty()) assert(false);


            super.curl(super._test_dev_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", ai.aiid));
        }
        catch(Exception ex) {assert (false);}
    }
    }



