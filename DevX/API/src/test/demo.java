import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by mauriziocibelli on 10/06/16.
 */
public class demo{

    class actor {
        String name;
        String URI;
    }
    public String readPage(String URI) throws IOException {
        String answer="";

        URL oracle = new URL("http://"+URI);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            answer = inputLine;
        in.close();

        return answer;

    }

    @Test
    public void chat() throws IOException {

        ArrayList<actor> actors = new ArrayList<>();

        actor a1 = new actor();
        a1.name = "sheldon";
        a1.URI = "54.174.17.31/similarity?aiid=sheldon&uid=sheldon&multiprocess=yes&nproc=8&q=";
        actors.add(a1);


        actor a2 = new actor();
        a2.name = "penny";
        a2.URI = "54.174.17.31/similarity?aiid=penny&uid=penny&multiprocess=yes&nproc=8&q=";
        actors.add(a2);

        actor a3 = new actor();
        a3.name = "leonard";
        a3.URI = "54.174.17.31/similarity?aiid=leonard&uid=leonard&multiprocess=yes&nproc=8&q=";
        actors.add(a3);

        actor a4 = new actor();
        a4.name = "ross";
        a4.URI = "54.174.17.31/similarity?aiid=ross&uid=ross&multiprocess=yes&nproc=8&q=";
        actors.add(a4);

        actor a5 = new actor();
        a5.name = "monica";
        a5.URI = "54.174.17.31/similarity?aiid=monica&uid=monica&multiprocess=yes&nproc=8&q=";
        actors.add(a5);

        actor a6 = new actor();
        a6.name = "phoebe";
        a6.URI = "54.174.17.31/similarity?aiid=phoebe&uid=phoebe&multiprocess=yes&nproc=8&q=";
        actors.add(a6);

        actor a7 = new actor();
        a7.name = "chandler";
        a7.URI = "54.174.17.31/similarity?aiid=chandler&uid=chandler&multiprocess=yes&nproc=8&q=";
        actors.add(a7);

        String q="love";
        String a="";
        while (true) {
            int random = (int )(Math. random() * actors.size());
            a = readPage(actors.get(random).URI+q.replace(" ","%20"));
            String asnwer = a.split("\\|")[1];
            Float prob = Float.valueOf(a.split("\\|")[0]);
            if (prob>0.2) {
                System.out.println(actors.get(random).name + ":" + asnwer);
                q = asnwer;
            }


        }




    }


}
