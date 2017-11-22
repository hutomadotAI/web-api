package test.functional;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.File;

import static io.restassured.RestAssured.given;

/**
 * Created by mauriziocibelli on 28/04/16.
 */

//curl -X GET "http://localhost:8080/v1/ai/9644bbc6-3ab1-464d-b677-f7c9ba19d75c/chat?min_p=0&history=&q=Fault%20reason%20is%207786%20WCDMA%20BASE%20STATION%20OUT%20OF%20USE%20" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8QE-jn7xhko6SsWlSUAxP39vT8fEVOPkZAODRF0gZaRrkmSZomthaGaom2SabGhqbmhhbpJqqlQLAAAA__8.HZBnPuBxLvSiQN-28mX9tb74Vvh1A48zlSFZemw08Bg"

public class base {

    public String admin_token = "eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8QE-jn7xhko6SsWlSUAxF1dffyMTCzPLVANzXYMUgzRdkzRjc90ko7RE3WSLpDSjJHNDY4OUFKVaAAAAAP__.7dc5arNyLKOUk6Df-DPSuddb5HD3enC3OaQGVMYhhys";

    public String POST_CREATE_AI = "http://localhost:8080/v1/ai";
    public String GET_GET_ALLAIs = "http://localhost:8080/v1/ai";
    public String GET_GET_SINGLEAI = "http://localhost:8080/v1/ai/__AIID__";
    public String DELETE_DELETE_AI = "http://localhost:8080/v1/ai/__AIID__";
    public String POST_UPLOAD_TRAINING = "http://localhost:8080/v1/ai/__AIID__/training";
    public String GET_TRAINING = "http://localhost:8080/v1/ai/__AIID__";

    public String GET_CHAT = "http://localhost:8080/v1/ai/__AIID__/chat";
    public String ADMIN_PUT_CREATE_DEV = "http://localhost:8080/v1/ai/api/admin?role=__ROLE__&devid=__DEVID__";
    public String ADMIN_DELET_DELET_DEV = "http://localhost:8080/v1/ai/admin?devid=__DEVID__";
    public String ADMIN_POST_KICKOFF_TRAINING = "http://api.hutoma.com/api2/admin/dl/__DEVID__/__AIID__?action=start";


    public String escape(String q) {
        return q.replace(" ", "%20");
    }

    public String createAI() {
        Response response = given().
                header("Authorization", "Bearer " + this.admin_token).
                when().
                post(this.POST_CREATE_AI).
                then().
                extract().response();

        return response.path("aiid").toString();
    }


    public String uploadTrainigFile(String aiid) {

        System.out.print(System.getProperty("user.home") + "/ai/test_files/contextual.txt");
        Response response = given().
                header("Authorization", "Bearer " + this.admin_token).
                contentType("multipart/form-data").
                multiPart("file", new File(System.getProperty("user.home") + "/ai/test_files/contextual.txt")).
                when().
                post(this.POST_UPLOAD_TRAINING.replace("__AIID__", aiid)).
                then().
                extract().response();
        return response.path("status.code").toString();

    }

    public boolean testTrainingStatus(String aiid) throws InterruptedException {

        for (int i = 0; i < 60; i++) {
            Response response =
                    given().
                            header("Authorization", "Bearer " + this.admin_token).
                            when().
                            get(this.GET_TRAINING.replace("__AIID__", aiid)).
                            then().
                            extract().response();
            String training_status = response.path("ai_status").toString();
            if (training_status.equals("training_in_progress")) return true;
            Thread.sleep(1000);
        }
        return false;
    }

    public boolean getRNNError(String aiid) throws InterruptedException {
        for (int i = 0; i < 240; i++) {
            Response response =
                    given().
                            header("Authorization", "Bearer " + this.admin_token).
                            when().
                            get(this.GET_TRAINING.replace("__AIID__", aiid)).
                            then().
                            extract().response();
            if (!response.path("deep_learning_error").toString().isEmpty()) return true;
            Thread.sleep(1000);
        }
        return false;
    }


    public String chat(String aiid, String q, String min_p, String history, String topic, boolean rnn_only) {
        Response response = given().
                header("Authorization", "Bearer " + this.admin_token).
                param(q, escape(q)).
                param("confidence_threshold", min_p).
                param("chat_history", history).
                param("current_topic", topic).
                param("rnn_only", rnn_only).
                contentType(ContentType.JSON).
                when().
                get(this.GET_CHAT.replace("__AIID__", aiid)).
                then().
                extract().response();
        return response.path("result.answer").toString();
    }

//
//
//    /**
//     * Checks if the user sentence contains a variable that is defined in an Intent
//     * @param dev_id
//     * @param _user_message
//     * @param e
//     * @return
//     */
//
//    public static String curl(String role, String method, String endpoint) throws IOException {
//        URL url = new URL(endpoint);
//        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestProperty("Authorization", "Bearer "+role);
//        con.setRequestMethod(method);
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//        while ((inputLine = in.readLine()) != null) response.append(inputLine);
//        in.close();
//        return response.toString();
//    }
//
//    private static String parseTrainingFile(ArrayList<String> training) {
//        String parsedFile="";
//        String currentSentence ="";
//        String previousSentence = "";
//        int ConversationCounter = 0;
//        try {
//            for (String s:training) {
//                currentSentence = s.trim();
//
//                // reset basic_ai history
//                if (s.isEmpty()) {
//                    ConversationCounter = 0;
//                    previousSentence = "";
//                }
//                else ConversationCounter ++;
//
//                // check if the conversation is longer than just answer and question
//                // if yes, and if the current sentence is a question, add the previous sentence
//                if ((ConversationCounter > 2) && (ConversationCounter & 1) != 0)
//                    currentSentence = currentSentence + "["+ previousSentence +"]";
//                else previousSentence = currentSentence;
//                parsedFile = parsedFile + currentSentence+"\n";
//
//            }
//        }
//        catch (Exception ex) {System.out.print(ex.getMessage());}
//        return  parsedFile;
//    }
//
//    public static boolean writeTextFile(String path,String aiid, String text) throws IOException {
//
//        try {
//            BufferedReader br = new BufferedReader(new StringReader(text.toLowerCase()));
//
//            ArrayList<String> source = new ArrayList<>();
//            ArrayList<String> target = new ArrayList<>();
//
//            String line = null;
//            boolean isQuestion = true;
//            while ((line = br.readLine()) != null) {
//                if ((line!=null)&&(!line.isEmpty())) {
//                    if (isQuestion) source.add(line + "\n");
//                    else target.add(line + "\n");
//                    isQuestion = !isQuestion;
//                }
//
//            }
//            br.close();
//            if (source.size()!=target.size()) {
//                System.out.print("size mismatch:"+source.size()+" "+target.size());
//                return false;
//            }
//
//
//            FileWriter writer = new FileWriter(path+"/q_"+aiid+".txt");
//            for(String str: source) writer.write(str);
//            writer.close();
//
//
//            writer = new FileWriter(path+"/a_"+aiid+".txt");
//            for(String str: target) writer.write(str);
//            writer.close();
//
//            return true;
//        }
//        catch (Exception ex) {System.out.println("parsing error:"+ex.getMessage());}
//        return false;
//
//    }
//
//    @BeforeClass
//    public static void init() throws IOException {
//
//
//        test.clean_test_data("","");
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        UUID guid = java.merge_QA_files.UUID.randomUUID();
//        String json = curl(role_admin, "POST", "http://54.83.145.18:8080/api/admin?role=" + Role.ROLE_PLAN_1 + "&plan_id=2&devid=HUTOMA_TEST" + guid);
//
//        api_root._myAIs _ai = new api_root._myAIs();
//        _ai = gson.fromJson(json,api_root._myAIs.class);
//        _test_dev_token =_ai.dev_token;
//
//
//        json = curl(_test_dev_token, "POST", "http://54.83.145.18:8080/api/ai");
//        api_root._ai _myai = new api_root._ai();
//        _myai = gson.fromJson(json,api_root._ai.class);
//
//        _test_client_token = _myai.client_token;
//        _test_aiid = _myai.aiid;
//        _test_devid = _ai.devid;
//
//    }
//
//    @AfterClass
//    public static void close() throws IOException {
//
//        //curl(role_admin, "DELETE", "http://54.83.145.18:8080/admin?devid=" + _test_devid);
//        test.clean_test_data(_test_devid,_test_aiid);
//        _test_dev_token ="";
//        _test_client_token = "";
//        _test_aiid = "";
//        _test_devid ="";
//
//
//
//
//    }


}
