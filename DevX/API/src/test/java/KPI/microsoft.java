package KPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 17/11/16.
 */
public class microsoft extends recallBaseClass {


    private static ArrayList<crecall> recall = new ArrayList<>();
    private static String base_uri = "https://westus.api.cognitive.microsoft.com/qnamaker/v1.0/knowledgebases/538895a6-6c8d-4a33-b829-fea34eda782b/generateAnswer";
    private String costa_devkey = "2981eb7eb3324c8d9b4c1971ab152ebf";


    public microsoft() throws IOException {
        super();
    }

    public static String curl(String method, String q) throws IOException {
        URL url = new URL(base_uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("question", q);
        con.setRequestProperty("Ocp-Apim-Subscription-Key", "20c97e7c061d403fa9739c35428212df");
        con.setRequestMethod(method);
        con.setDoOutput(true);
        String body = "question=" + q;
        if (!body.isEmpty()) {
            byte[] postData = body.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            OutputStream os = con.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

    public void Recall(String costa_ground_truth_file, String devkey) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(costa_ground_truth_file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        long startTime = System.currentTimeMillis();
        long start = System.nanoTime();
        String json = "";
        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);
                String q = row.getCell(1).getStringCellValue().toLowerCase();
                String gt = row.getCell(2).getStringCellValue().toLowerCase();
                json = curl("POST", q);
                MSApi cr = gson.fromJson(json, MSApi.class);
                recall.add(new crecall(q, cr.answer.replace("\n", ""), gt));
                super.printProgress(startTime, rows, rowindex);
                Thread.sleep(5000);
            } catch (Exception e) {
                System.out.println("json:" + json);
            }
        }
        printRecall(start, recall);

    }

    @Test
    public void RecallCosta() throws IOException {
        Recall(super.costa_ground_truth_file, this.costa_devkey);
    }


    public class MSApi {
        String answer;
        String score;

    }


}
