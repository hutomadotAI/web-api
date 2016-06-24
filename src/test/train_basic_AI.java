import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.ai.api_root;
import org.junit.Test;

import java.io.IOException;

import static hutoma.api.server.db.query.get_ai_status;


public class train_basic_AI extends base_test {


    @Test
    public void create_and_delete_its_own_ai() throws IOException, InterruptedException {

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


        int counter = 0;
        String stat ="";
        while (counter < 120) {
            stat = get_ai_status(ai.aiid);
            if (stat.equals(String.valueOf(msg.training_queued))) break;
            Thread.sleep(1000);
            System.out.println("waiting...(state was:"+stat+")");
            counter ++;
        }

        if (stat.isEmpty()) assert(false);

        json = super.curl(super._test_dev_token, "GET", _curl_GET_CHAT.replace("__AIID__", ai.aiid)+"?q=hello");
        api_root._chat response = new api_root._chat();
        response =  gson.fromJson(json, api_root._chat.class);

        assert(response.result.answer.equals("how are you\n"));
        //now deletes the AI created
        super.curl(super._test_dev_token, "DELETE", _curl_DELETE_DELETE_AI.replace("__AIID__", ai.aiid));


    }
}