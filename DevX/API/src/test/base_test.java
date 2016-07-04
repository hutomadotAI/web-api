import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.Role;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.test;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 28/04/16.
 */
public class base_test {

    public static String role_admin ="eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c";

    protected static String _test_devid="";
    protected static String _test_aiid="";
    protected static String _test_dev_token="";
    protected static String _test_client_token="";

    protected String _curl_POST_CREATE_AI ="http://54.83.145.18:8080/api/ai/";
    protected String _curl_GET_GET_ALLAIs ="http://54.83.145.18:8080/api/ai/";
    protected String _curl_GET_GET_SINGLEAI ="http://54.83.145.18:8080/api/ai/__AIID__";
    protected String _curl_DELETE_DELETE_AI ="http://54.83.145.18:8080/api/ai/__AIID__";
    protected String _curl_PUT_UPLOAD_TRAINING ="http://54.83.145.18:8080/api/ai/__AIID__/training";
    protected String _curl_GET_CHAT ="http://54.83.145.18:8080/api/ai/__AIID__/chat";
    protected String _curl_ADMIN_PUT_CREATE_DEV ="http://54.83.145.18:8080/api/admin?role=__ROLE__&devid=__DEVID__";
    protected String _curl_ADMIN_DELET_DELET_DEV ="http://54.83.145.18:8080/api/admin?devid=__DEVID__";

    protected String _curl_ADMIN_POST_KICKOFF_TRAINING ="http://54.224.70.223:8080/api2/admin/dl/__DEVID__/__AIID__?action=start";



    public static String curl(String role, String method, String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", "Bearer "+role);
        con.setRequestMethod(method);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }


    private static String parseTrainingFile(ArrayList<String> training) {
        String parsedFile="";
        String currentSentence ="";


        String previousSentence = "";
        int ConversationCounter = 0;

        try {
            for (String s:training) {
                currentSentence = s;

                // reset chat history
                if (s.isEmpty()) {
                    ConversationCounter = 0;
                    previousSentence = "";
                }
                else ConversationCounter ++;

                // check if the conversation is longer than just answer and question
                // if yes, and if the current sentence is a question, add the previous sentence
                if ((ConversationCounter > 2) && (ConversationCounter & 1) != 0)
                    currentSentence = "CMDHSTART "+ previousSentence +" CMDHEND " + currentSentence;
                else previousSentence = currentSentence;

                parsedFile = parsedFile + currentSentence+"\n";

            }
        }
        catch (Exception ex) {parsedFile="";}

        return  parsedFile;
    }

    @BeforeClass
    public static void init() throws IOException {

//
//        ArrayList<String> tests = new ArrayList<>();
//        tests.add("Hello");
//        tests.add("Hi, how are you");
//        tests.add("I am fine thanks and you?");
//        tests.add("I am ok thanks");
//        tests.add("");
//        tests.add("I am fine thanks and you?");
//        tests.add("I am OK but i never really asked how are you?");
//        tests.add("");
//        tests.add("thanks");
//        tests.add("you are welcome");
//
//        String t = parseTrainingFile(tests);
//

        test.clean_test_data();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        UUID guid = java.util.UUID.randomUUID();
        String json = curl(role_admin, "POST", "http://54.83.145.18:8080/api/admin?role=" + Role.ROLE_PLAN_1 + "&plan_id=2&devid=HUTOMA_TEST" + guid);

        api_root._myAIs _ai = new api_root._myAIs();
        _ai = gson.fromJson(json,api_root._myAIs.class);
        _test_dev_token =_ai.dev_token;


        json = curl(_test_dev_token, "POST", "http://54.83.145.18:8080/api/ai");
        api_root._ai _myai = new api_root._ai();
        _myai = gson.fromJson(json,api_root._ai.class);

        _test_client_token = _myai.client_token;
        _test_aiid = _myai.aiid;
        _test_devid = _ai.devid;

    }

    @AfterClass
    public static void close() throws IOException {

        curl(role_admin, "DELETE", "http://54.83.145.18:8080/admin?devid=" + _test_devid);
        _test_dev_token ="";
        _test_client_token = "";
        _test_aiid = "";
        _test_devid ="";
        test.clean_test_data();



    }


}
