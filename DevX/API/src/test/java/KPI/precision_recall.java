package KPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mauriziocibelli on 29/08/16.
 */
public class precision_recall  {

    private String q_path = System.getProperty("user.home") + "/ai/test_files/precision_recall.xls";
    // private String watson="curl -X POST -u \"2677e839-b392-45ff-97c9-07e99c2695ef\":\"Hr154xulLdJv\" -H \"Content-Type:application/json\" -d \"{\\\"input\\\": {\\\"text\\\": \\\"__QUERY__\\\"}}\" \"https://gateway.watsonplatform.net/conversation/api/v1/workspaces/444b2461-1cca-43fc-afaa-a820ffcd9b03/message?version=2016-07-11\"";
    private String DEVID = "DEMO24869e07-0d0f-4f37-b2fa-c8bf2b7130dd";
    private String AIID = "deck";
    private String UID = "uid";


    Logger logger;
    JsonSerializer serializer = new JsonSerializer();
    private final String LOGFROM = "wnetconnector";


    public class _metadata {
        public String intentName;
    }

    public class _result {
        String resolvedQuery;
        _metadata metadata;

    }

    public class _apiai {
        _result result;

    }


    public ChatResult getAnswer(String devid, String aiid, String uid, String topic, String q, float min_p) throws SemanticAnalysis.SemanticAnalysisException {
        try {
            UrlBuilder url = UrlBuilder.fromString("http://52.2.184.105/similarity")
                    .addParameter("q", q)
                    .addParameter("aiid", aiid)
                    .addParameter("dev_id", devid)
                    .addParameter("uid", uid)
                    .addParameter("min_p", Float.toString(min_p))
                    .addParameter("multiprocess", "yes")
                    .addParameter("topic", topic)
                    .addParameter("nproc", "8");

            URL finalUrl = new URL(url.toString());
            InputStream stream = finalUrl.openStream();
            ChatResult result = (ChatResult) serializer.deserialize(stream, ChatResult.class);
            return result;
        } catch (JsonParseException jp) {
            logger.logError(LOGFROM, "failed to deserialize json result from SemanticAnalysis server: ");
            throw new SemanticAnalysis.SemanticAnalysisException(jp);
        } catch (IOException e) {
            logger.logError(LOGFROM, "failed to contact SemanticAnalysis server: " + e.toString());
            throw new SemanticAnalysis.SemanticAnalysisException(e);
        }
    }


    private ChatResult getChat(String q) throws SemanticAnalysis.SemanticAnalysisException {
        ChatResult semanticAnalysisResult = getAnswer(DEVID, AIID, UID, "", q, 0);
        return semanticAnalysisResult;
    }

    public  String curl(String role, String method, String endpoint) throws IOException {
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


    @Test
    public void RecallHutoma() throws IOException {


        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(q_path));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        double good = 0.0;
        double not_good = 0.0;


        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);
                String q = row.getCell(0).getStringCellValue().toLowerCase();
                String a = row.getCell(1).getStringCellValue().toLowerCase();
                ChatResult apiResult = getChat(q);
                if (apiResult.getAnswer().replace("\n", "").equals(a))
                    System.out.println("1\t" + (++good / (good + not_good) * 100) + "%");
                else System.out.println("0\t" + (good / (good + ++not_good) * 100) + "%");
            } catch (Exception e) {}

        }
    }



    @Test
    public void RecallApiAI() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(q_path));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        double good = 0.0;
        double not_good = 0.0;

        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);
                String q = row.getCell(0).getStringCellValue().toLowerCase();
                String a = row.getCell(1).getStringCellValue().toLowerCase();
                String json = curl("0ebeef8c3eab47c0bf3d4eb0bdc337ab", "GET", "https://api.api.ai/api/query?v=20150910&lang=en&query=" + q.replace(" ", "%20"));
                _apiai cr = new _apiai();
                cr = gson.fromJson(json, _apiai.class);
                if (cr.result.metadata.intentName.replace("\n", "").equals(a))
                    System.out.println("1\t" + (++good / (good + not_good) * 100) + "%");
                else System.out.println("0\t" + (good / (good + ++not_good) * 100) + "%");
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void recallWatson() throws IOException, InterruptedException, SemanticAnalysis.SemanticAnalysisException {

//            // upload training file
//            ProcessBuilder pb = new ProcessBuilder("curl", "-X","POST", "https://gateway.watsonplatform.net/conversation/api/v1/workspaces/444b2461-1cca-43fc-afaa-a820ffcd9b03/message?version=2016-07-11","-u","\"2677e839-b392-45ff-97c9-07e99c2695ef\":\"Hr154xulLdJv\"","-H","\"Content-Type:application/json\"","-d","\"{\\\"input\\\": {\\\"text\\\": \\\""+q.replace(" ","%20")+"\\\"}}\"");
//
//
//            System.out.print(pb.command().toString());
//            pb.redirectErrorStream(true); // This is the important part
//            pb.redirectOutput(new File("curloutput.txt"));
//            Process p = pb.start();
//
//            InputStreamReader isr = new  InputStreamReader(p.getInputStream());
//            BufferedReader br = new BufferedReader(isr);
//
//            String lineRead;
//            while ((lineRead = br.readLine()) != null) {
//                // swallow the line, or print it out - System.out.println(lineRead);
//            }
    }


}
