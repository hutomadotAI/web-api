package KPI;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.sub.ChatResult;
import io.mikael.urlbuilder.UrlBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by mauriziocibelli on 29/08/16.
 */
public class wnet extends recallBaseClass {
    static JsonSerializer serializer = new JsonSerializer();
    private static ArrayList<crecall> recall = new ArrayList<>();
    private final String LOGFROM = "wnetconnector";
    private String costa_dev_id = "devid_costa";
    private String costa_ai_id = "aiid_costa";
    private String nhs_dev_id = "devid_nhs";
    private String nhs_ai_id = "aiid_nhs";
    private String WNET_URL = "http://54.209.201.97:8888/ai/%s/%s/chat";


    public wnet() throws IOException {
        super();
    }

    public void Recall(String ground_truth_file, String bot_url) throws IOException, InterruptedException {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(ground_truth_file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        long startTime = System.currentTimeMillis();
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(connectionManager);
        long start = System.nanoTime();
        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);

                String history = "";
                if (row.getCell(0) != null)
                    history = row.getCell(0).getStringCellValue().toLowerCase();

                String q = "";
                if (row.getCell(1) != null)
                    q = row.getCell(1).getStringCellValue().toLowerCase();

                String gt = "";
                if (row.getCell(2) != null)
                    gt = row.getCell(2).getStringCellValue().toLowerCase();

                UrlBuilder url = UrlBuilder.fromString(bot_url)
                        .addParameter("q", q)
                        .addParameter("min_p", "0")
                        .addParameter("history", history);
                GetMethod get = new GetMethod(url.toString());
                try {
                    ChatResult result = null;
                    int statusCode = httpClient.executeMethod(get);
                    if (statusCode != HttpStatus.SC_OK) {
                        System.err.println("Method failed: " + get.getStatusLine());
                    }
                    String response = get.getResponseBodyAsString();
                    result = new ChatResult((ChatResult) serializer.deserialize(response, ChatResult.class));
                    String a = result.getAnswer();
                    recall.add(new crecall(q, a, gt));
                    printProgress(startTime, rows, rowindex);
                } catch (HttpException e) {
                    System.err.println("Fatal protocol violation: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Fatal transport error: " + e.getMessage());
                } finally {
                    get.releaseConnection();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        printRecall(start, recall);
    }

    @Test
    public void RecallCosta() throws IOException, InterruptedException {
        Recall(super.costa_ground_truth_file, String.format(this.WNET_URL, this.costa_dev_id, this.costa_ai_id));
    }

    @Test
    public void RecallNHS() throws IOException, InterruptedException {
        Recall(super.nhs_ground_truth_file, String.format(this.WNET_URL, this.nhs_dev_id, this.nhs_ai_id));
    }

}
