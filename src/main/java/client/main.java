package client;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by mauriziocibelli on 26/04/16.
 */
public class main {

    static class wnet_search implements Runnable {


        private static String WNETSERVERFAST = "http://54.210.59.224:8000/similarity?";
        private double score;
        private String q1;
        private String q2;
        wnet_search(String _q1, String _q2) {
            q1 = _q1;
            q2 = _q2;
        }

        public double get_score() {
            return score;
        }

        private void getWnet() throws IOException {
            try {

                long start = System.currentTimeMillis();
                URL url = new URL(WNETSERVERFAST+"q="+q1.replace(" ","%20")+"&a="+q2.replace(" ","%20"));
                String wnet_res = IOUtils.toString(new InputStreamReader(url.openStream()));
                score = Double.valueOf(wnet_res);
                System.out.println(System.currentTimeMillis()-start+"["+score+"]"+q1+"/"+q2);

            } catch (IOException e) {
            }
        }

        @Override
        public void run() {
            try {
                getWnet();
            } catch (Exception ex) {}
        }


    }

    public static void main(String[] args) throws InterruptedException {

        try
        {
            double current_max=0;
            String final_answer="";

            try (BufferedReader br = new BufferedReader(new FileReader("/Users/mauriziocibelli/Desktop/q_costa.txt"))) {
                String line;

                while ((line = br.readLine()) != null) {
                    String q="do you have baby sitters on board";
                    wnet_search ws = new wnet_search(q,line);
                    Thread t = new Thread(ws);
                    t.run();
                    //t.join();

                    if (ws.get_score()>current_max) {
                        current_max = ws.get_score();
                        final_answer = line;
                    }


                }
            }


//
//            String q="do you have baby sitters on board";
//            String wnet_server = utils.getConfigProp("wnet_server");
//            wnet_server += "&q="+q.replace(" ","%20");
//            wnet_server += "&aiid=costa";
//            wnet_server += "&uid=costa";
//            wnet_server += "&min_p=0";
//            URL url = new URL(wnet_server);
//            long start = System.currentTimeMillis();
//
//            System.out.println(IOUtils.toString(new InputStreamReader(url.openStream())));
//            System.out.println((System.currentTimeMillis()-start));
        }
        catch (IOException e) {}
    }
}
