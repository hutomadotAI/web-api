import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.ai.api_root;
import org.junit.Test;

import java.io.IOException;

import static hutoma.api.server.db.ai.get_ai_status;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class deep_learning_test extends base_test {

    @Test
    public void kickoff_dl_training() throws IOException {
        try {


            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //creates a new AI
            String json = super.curl(super._test_dev_token, "POST", _curl_POST_CREATE_AI);
            api_root._ai ai = new api_root._ai();
            ai = gson.fromJson(json, api_root._ai.class);
            assert (!ai.aiid.isEmpty());


            // upload training file
            ProcessBuilder pb = new ProcessBuilder("curl", "-X","POST","-H","Authorization: Bearer "+_test_dev_token,_curl_PUT_UPLOAD_TRAINING.replace("__AIID__", ai.aiid),"-F","file=@"+System.getProperty("user.home") + "/ai/sampletraining.txt");
            System.out.print(pb.command().toString());
            Process p = pb.start();


        //    super.curl(super.role_admin, "POST", _curl_ADMIN_POST_KICKOFF_TRAINING.replace("__DEVID__", super._test_devid).replace("__AIID__", ai.aiid));



            int counter = 0;
            String stat ="";
            while (counter < 120) {
                stat = get_ai_status(ai.aiid);

                if (stat.equals(String.valueOf(msg.training_in_progress))) break;
                Thread.sleep(1000);
                counter ++;
            }





            json = super.curl(super._test_dev_token, "GET", _curl_GET_CHAT.replace("__AIID__",ai.aiid)+"?q=i%20want%20a%20cake&uid=123&min_p=1");

            if (stat.isEmpty()) assert(false);





            super.curl(super._test_dev_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", ai.aiid));
        }
        catch(Exception ex) {assert (false);}
    }




}
