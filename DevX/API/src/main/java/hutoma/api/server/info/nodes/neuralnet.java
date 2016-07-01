package hutoma.api.server.info.nodes;

import hutoma.api.server.utils.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
public class neuralnet {

    private void startRNNServer(String dev_id, String aiid) throws IOException, InterruptedException {

        try {

            ProcessBuilder pb = new ProcessBuilder(utils.getConfigProp("pythonPath"),
                    utils.getConfigProp("rnnServerPath"),
                    "--normalize",
                    "--beam-search",
                    "--state",
                    utils.getConfigProp("rnnServerParams1").replace("__USERID__", dev_id).replace("__BOTID__", aiid),
                    utils.getConfigProp("rnnServerParams2").replace("__USERID__", dev_id).replace("__BOTID__", aiid),
                    "--botid",
                    aiid);

            Map<String, String> env = pb.environment();
            env.put("THEANO_FLAGS", "floatX=float32,device=gpu,nvcc.fastmath=True");
            env.put("PYTHONPATH", "/home/ubuntu/caffe/python:/usr/local/bin:/home/ubuntu/python/hutoma:/home/ubuntu/python/hutoma/neuralnetwork:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets/rnn:/home/ubuntu/python/hutoma/neuralnetwork/groundhog:/home/ubuntu/python/hutoma/neuralnetwork/groundhog/dataset:/home/ubuntu/python/hutoma/neuralnetwork/groundhog/layers:/home/ubuntu/python/hutoma/neuralnetwork/groundhog/models:/home/ubuntu/python/hutoma/neuralnetwork/groundhog/trainer:/home/ubuntu/python/hutoma/neuralnetwork/groundhog/utils");
            env.put("LD_LIBRARY_PATH", "/usr/local/cuda/bin:/usr/local/cuda/lib64:$LD_LIBRARY_PATH");
            env.put("PATH", "/usr/local/cuda-6.5/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games");
            pb.directory(new File(utils.getConfigProp("rnnRoot")));
            if (utils.getConfigProp("runlocal").equals("true")) {
                File log1 = new File("~/ai/"+dev_id+"/"+aiid+"/pythonLog.txt");
                File log2 = new File("~/ai/"+dev_id+"/"+aiid+"/pythonErrorLog.txt");
                pb.redirectError(ProcessBuilder.Redirect.appendTo(log1));
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log2));
                pb.redirectOutput();
                pb.redirectError();
            }
            pb.start();
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
