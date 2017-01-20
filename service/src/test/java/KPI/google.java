package KPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 17/11/16.
 */
public class google extends recallBaseClass {


    private static ArrayList<crecall> recall = new ArrayList<>();
    private String costa_devkey = "2981eb7eb3324c8d9b4c1971ab152ebf";
    private String nhs_devkey = "f43d2ec0a0d5423d9b0187b7a204e997";
    private String baseUri = "https://api.api.ai/v1/query?v=20150910&timezone=Europe/London&lang=en&latitude=37.459157&longitude=-122.17926&sessionId=1234567890&query=";


    public google() throws IOException {
        super();
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
                json = curl(devkey, "GET", this.baseUri + q.replace(" ", "%20"), "");
                Apiai cr = gson.fromJson(json, Apiai.class);
                if (cr.result.metadata.intentName != null)
                    recall.add(new crecall(q, cr.result.metadata.intentName.replace("\n", ""), gt));
                else recall.add(new crecall(q, "", gt));
                super.printProgress(startTime, rows, rowindex);
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

    @Test
    public void RecallNHS() throws IOException {
        Recall(super.nhs_ground_truth_file, this.nhs_devkey);
    }


    public class Metadata {
        public String intentName;
    }

    public class Result {
        String resolvedQuery;
        Metadata metadata;

    }

    public class Apiai {
        Result result;

    }


}
