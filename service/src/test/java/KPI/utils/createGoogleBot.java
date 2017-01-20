package KPI.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mauriziocibelli on 01/12/16.
 */
public class createGoogleBot {


    private static Collection<intent> getIntents(String devtoken) {
        Gson gson = new Gson();
        String json = curl_exec(new String[]{"curl", "-k", "-H", "Authorization: Bearer " + devtoken,
                "https://api.api.ai/v1/intents?v=20150910"});
        Type collectionType = new TypeToken<Collection<intent>>() {
        }.getType();
        Collection<intent> enums = gson.fromJson(json, collectionType);
        return enums;
    }

    private static void deleteIntents(String devtoken, Collection<intent> intent_list) {

        for (intent item : intent_list) {
            Gson gson = new Gson();
            String json = curl_exec(new String[]{"curl", "-k", "-X", "DELETE", "-H",
                    "Authorization: Bearer " + devtoken,
                    String.format("https://api.api.ai/v1/intents/%s?v=20150910", item.id)});
            status stat = gson.fromJson(json, status.class);
            if (stat.status.code != 200) {
                System.out.println("Error while deleting intent id:" + item.id);
                break;
            }


        }

    }

    private static void createIntents(String devtoken, String intentName, String UserSay, String Response) {
        Gson gson = new Gson();
        String json = curl_exec(new String[]{"curl", "-k", "-H", "Content-Type: application/json; charset=utf-8",
                "-H",
                "Authorization: Bearer " + devtoken,
                "--data",
                String.format("{'name':'%s','userSays':[{'data':[{'text':'%s'}],'speech':'%s'}],'priority':500000}", intentName.replace("'", ""), UserSay.replace("'", ""), Response.replace("'", "")),
                "https://api.api.ai/v1/intents?v=20150910"});
        System.out.println("Intent:" + intentName + "[" + UserSay + "]");
        System.out.println(json);
        System.out.println("");

    }

    private static String curl_exec(String[] command) {

        ProcessBuilder process = new ProcessBuilder(command);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            return result;

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        }
        return "";
    }

    private static void resetAiIntents(String devtoken) {
        deleteIntents(devtoken, getIntents(devtoken));
    }


    public static void main(String[] args) throws IOException {
        String botname = args[0]; // botid
        String devtoken = args[1];  // dev token associated to botid
        String qa_file = args[2]; // path to Q file

        // remove all existing intents
        resetAiIntents(devtoken);

        // reads new intents
        BufferedReader in = new BufferedReader(new FileReader(qa_file));
        String str;
        List<String> Qs = new ArrayList<>();
        List<String> As = new ArrayList<>();


        // creates Q & A list
        boolean isQ = true;
        while ((str = in.readLine()) != null) {
            if (str.isEmpty()) continue;
            if (isQ) Qs.add(str);
            else As.add(str);
            isQ = !isQ;
        }

        // creates a new intent
        for (int i = 0; i < Qs.size(); i++) {
            String question = Qs.get(i);
            String answer = As.get(i);
            String intent_name = answer;
            createIntents(devtoken, intent_name, question, answer);

        }


    }

    // Google Intents
    private static class intent {
        String id;
        String name;
    }

    private static class response {
        int code;
        String errorType;
    }

    private static class status {
        response status;

    }

}
