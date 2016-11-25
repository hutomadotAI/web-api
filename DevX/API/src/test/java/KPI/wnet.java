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
    private static ArrayList<wnetrecall> recall = new ArrayList<>();
    private final String LOGFROM = "wnetconnector";
    private String DEVID = "5129a38c-735f-4608-a9ca-377d2b06c868";
    private String AIID = "34126944-f3b9-4a9a-9f38-eeae29800afa";
    private String UID = "1234";
    private String WNET_URI = "http://52.2.184.105/similarity";

    public wnet() throws IOException {
        super();
    }

    @Test
    public void RecallHutoma() throws IOException, InterruptedException {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(super.ground_truth_file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        double good = 0.0;
        double not_good = 0.0;
        long startTime = System.currentTimeMillis();
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpClient httpClient = new HttpClient(connectionManager);
        ArrayList<CreateThread> threadsList = new ArrayList<>();
        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);
                String history = "";
                if (row.getCell(0) != null)
                    history = row.getCell(0).getStringCellValue().toLowerCase();
                String q = row.getCell(1).getStringCellValue().toLowerCase();
                String gt = row.getCell(2).getStringCellValue().toLowerCase();
                UrlBuilder url = UrlBuilder.fromString(this.WNET_URI)
                        .addParameter("q", q)
                        .addParameter("aiid", this.AIID)
                        .addParameter("dev_id", this.DEVID)
                        .addParameter("uid", this.UID)
                        .addParameter("min_p", "0")
                        .addParameter("multiprocess", "yes")
                        .addParameter("history", history)
                        .addParameter("nproc", "16");
                GetMethod get = new GetMethod(url.toString());
                CreateThread t = new CreateThread(httpClient, get, startTime, rows, rowindex, "" + rowindex, q, gt);
                threadsList.add(t);
                t.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        for (int i = 0; i < threadsList.size(); i++) {
            threadsList.get(i).join();
        }

        for (int i = 0; i < recall.size(); i++) {
            if (recall.get(i).a.replace("\n", "").equals(recall.get(i).groundTruth)) good++;
            else {
                not_good++;
                System.out.println("Q:" + recall.get(i).q + ", A:" + recall.get(i).a + ", GT:" + recall.get(i).groundTruth);
            }
        }

        System.out.println("LINES TOTAL=" + good + not_good);
        System.out.println("CORRECT    =" + good);
        System.out.println("INCORRECT    =" + not_good);
        System.out.println("RECALL @1  =" + (good / (good + not_good) * 100) + "%");

    }


    static class CreateThread extends Thread {
        private final HttpClient httpClient;
        private final GetMethod method;
        private final String q;
        private final String groundTruth;
        private long startTime;
        private int rows;
        private int rowindex;

        public CreateThread(HttpClient httpClient, GetMethod method, long startTime, int rows, int rowindex, String name, String q, String groundTruth) {
            super(name);
            this.httpClient = httpClient;
            this.method = method;
            this.q = q;
            this.groundTruth = groundTruth;
            this.startTime = startTime;
            this.rowindex = rowindex;
            this.rows = rows;
        }

        @Override
        public void run() {
            try {
                ChatResult result = null;
                int statusCode = this.httpClient.executeMethod(this.method);
                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: " + this.method.getStatusLine());
                }
                String response = this.method.getResponseBodyAsString();
                result = new ChatResult((ChatResult) serializer.deserialize(response, ChatResult.class));
                String a = result.getAnswer();
                recall.add(new wnetrecall(this.q, a, this.groundTruth));
                printProgress(this.startTime, this.rows, this.rowindex);
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
            } finally {
                this.method.releaseConnection();
            }
        }

    }

    public static class wnetrecall {
        String q;
        String a;
        String groundTruth;

        public wnetrecall(String q, String a, String groundTruth) {
            this.q = q;
            this.a = a;
            this.groundTruth = groundTruth;
        }
    }

}
