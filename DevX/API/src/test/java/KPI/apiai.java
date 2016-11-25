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

/**
 * Created by mauriziocibelli on 17/11/16.
 */
public class apiai extends recallBaseClass {


    public apiai() throws IOException {
        super();
    }

    @Test
    public void RecallApiAI() throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(super.ground_truth_file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        int rows = sheet.getPhysicalNumberOfRows();
        int rowindex = 1;
        double good = 0.0;
        double not_good = 0.0;
        long startTime = System.currentTimeMillis();

        while (rowindex < rows) {
            try {
                row = sheet.getRow(rowindex++);
                String q = row.getCell(0).getStringCellValue().toLowerCase();
                String a = row.getCell(1).getStringCellValue().toLowerCase();
                String json = curl("0ebeef8c3eab47c0bf3d4eb0bdc337ab", "GET", "https://api.api.ai/api/query?v=20150910&lang=en&query=" + q.replace(" ", "%20"));
                Apiai cr = new Apiai();
                cr = gson.fromJson(json, Apiai.class);
                if (cr.result.metadata.intentName.replace("\n", "").equals(a)) good++;
                else not_good++;
                super.printProgress(startTime, rows, rowindex);
            } catch (Exception e) {
            }
        }
        System.out.println("LINES TOTAL=" + good + not_good);
        System.out.println("CORRECT    =" + good);
        System.out.println("INCORRECT    =" + not_good);
        System.out.println("RECALL @1  =" + (good / (good + not_good) * 100) + "%");
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
