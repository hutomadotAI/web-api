package KPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by mauriziocibelli on 17/11/16.
 */
public class recallBaseClass {

    private static final String RECALL_FILES_DIR = "recall-tests/";
    static Logger logger = Logger.getLogger("recall");
    private final PrintStream stdout = System.out;
    public String costa_ground_truth_file;
    public String nhs_ground_truth_file;

    public recallBaseClass() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        this.costa_ground_truth_file = classLoader.getResource(RECALL_FILES_DIR + "costa_bot_recall_test.xls").getPath();
        this.nhs_ground_truth_file = classLoader.getResource(RECALL_FILES_DIR + "nhs_bot_recall_test.xls").getPath();
    }

    public static void printRecall(long start, ArrayList<crecall> recall) {
        double good = 0;
        double not_good = 0;
        long diff = System.nanoTime() - start;
        double secs = diff / 1000000000.0;
        double min = secs / 60;
        System.out.println("--- INCORRECT RESULTS ---\n");
        for (int i = 0; i < recall.size(); i++) {
            String answer = "";
            String ground_truth = "";

            if (recall.get(i).a != null)
                answer = recall.get(i).a.toLowerCase();

            if (recall.get(i).groundTruth != null)
                ground_truth = recall.get(i).groundTruth.toLowerCase();

            if (answer.equals(ground_truth)) good++;
            else {
                not_good++;
                System.out.println(recall.get(i).q + "," + recall.get(i).a + "," + recall.get(i).groundTruth);
            }
        }
        System.out.println("---------------------------------");
        System.out.println("TOT MIN:" + min + "(" + secs + ")");
        System.out.println("LINES TOTAL=" + (good + not_good));
        System.out.println("CORRECT    =" + good);
        System.out.println("INCORRECT  =" + not_good);
        System.out.println("RECALL @1  =" + (good / (good + not_good) * 100) + "%");
    }

    public static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));
        logger.info(string.toString().replace("\n", ""));
    }

    protected static void addPostBody(final HttpURLConnection con, final String body) throws IOException {
        if (!body.isEmpty()) {
            byte[] postData = body.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            OutputStream os = con.getOutputStream();
            os.write(postData);
            os.flush();
            os.close();
        }

    }

    public static String curl(String role, String method, String endpoint, String body) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization", "Bearer " + role);
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setRequestProperty("Accept", "application/json");

        con.setRequestMethod(method);
        con.setDoOutput(true);

        addPostBody(con, body);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) response.append(inputLine);
        in.close();
        return response.toString();
    }

    public static class crecall {
        String q;
        String a;
        String groundTruth;

        public crecall(String q, String a, String groundTruth) {
            this.q = q;
            this.a = a;
            this.groundTruth = groundTruth;
        }
    }
}
