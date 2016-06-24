package hutoma.api.server.info.nodes;

import java.io.IOException;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class neuralnet {

    private void startRNNServer(String dev_id, String aiid) throws IOException, InterruptedException {

        try {

          // NOT IMPLEMENTED YET
        }
        catch (Exception ex) {}

    }

    public static String process(String dev_id, String aiid,String q) {
        String res = "";

//        try {
//
//            InetAddress host = InetAddress.getLocalHost();
//            Socket socket = null;
//            int port = utils.db.getRNNSocket(aiid);
//
//            if (port < 0) return "";
//            socket = new Socket(host.getHostName(), port);
//            socket.setSoTimeout(10000);
//            DataOutputStream outToServer2 = new DataOutputStream(socket.getOutputStream());
//            outToServer2.writeBytes(q + "####");
//            String response = "";
//            try (InputStream is = socket.getInputStream()) {
//                BufferedReader lines = new BufferedReader(new InputStreamReader(is));
//                response = lines.readLine();
//            } catch (Exception ex) {
//            }
//
//            outToServer2.close();
//            res = response.toString();
//        } catch (Exception ex) {}
        return res;
    }
}
