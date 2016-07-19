package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.db.RNN;
import hutoma.api.server.db.ai;
import hutoma.api.server.db.dev;
import hutoma.api.server.utils.utils;
import org.apache.commons.io.FileUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by mauriziocibelli on 11/05/16.
 */
@Path("/admin/")
public class deep_learning {


    private int _RNN_ERROR = -1;
    private int _RNN_QUEUED = 0;
    private int _RNN_STARTED = 1;
    private int _RNN_DONE = 2;
    private int _RNN_STOPPING = 3;
    private int _RNN_STOPPED = 4;
    private int _RNN_MAXED = 5;
    private int _RUNNING = 1;
    private int _NOTRUNNING = 0;
    private static int getWordCount(String filepath) throws IOException {
        try {
            File file = new File(filepath);
        return Integer.parseInt(FileUtils.readFileToString(file).replace("\n", ""));
        }
        catch (Exception ex) {}
        return 0;
    }
    private static void prepTrainingFiles(String dev_id,String aiid) throws IOException, InterruptedException, SQLException, ClassNotFoundException {


        try {
            File f = new File(utils.getConfigProp("botdir") + dev_id + "/" + aiid + "/binarized_text.target.shuff.h5");

            if (!f.exists()) {
                ProcessBuilder pb = new ProcessBuilder("python3.4",
                        "/home/ubuntu/python/hutoma/neuralnetwork/neuralnets/rnn/preprocess/main.py",
                        "/home/ubuntu/ai/" + dev_id + "/" + aiid + "/",
                        "source",
                        "target");

                Map<String, String> env = pb.environment();
                env.put("THEANO_FLAGS", "floatX=float32,device=gpu,nvcc.fastmath=True");
                env.put("PYTHONPATH", "/home/ubuntu/caffe/python:/usr/local/bin:/home/ubuntu/python/hutoma:/home/ubuntu/python/hutoma/neuralnetwork:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets/rnn:/home/ubuntu/python/hutoma/neuralnetwork/core:/home/ubuntu/python/hutoma/neuralnetwork/core/dataset:/home/ubuntu/python/hutoma/neuralnetwork/core/layers:/home/ubuntu/python/hutoma/neuralnetwork/core/models:/home/ubuntu/python/hutoma/neuralnetwork/core/trainer:/home/ubuntu/python/hutoma/neuralnetwork/core/utils");
                env.put("LD_LIBRARY_PATH", "/usr/local/cuda/bin:/usr/local/cuda/lib64:$LD_LIBRARY_PATH");
                env.put("PATH", "/usr/local/cuda-6.5/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games");
                pb.directory(new File(utils.getConfigProp("rnnroot") + "preprocess/"));
                File log = new File("/home/ubuntu/ai/" + dev_id + "/" + aiid + "/prepfile_log" + "_" + aiid + ".txt");
                pb.redirectErrorStream(true);
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));

                Process p = pb.start();
                p.waitFor();
            }
        }
        catch (Exception e) { utils.debug("prep training exeptionç:"+e.getMessage());

        }

    }
    private static void kick_off_training(String dev_id,String aiid) throws SQLException, ClassNotFoundException {
       try {

           // prepares training files.
           String script =  utils.getConfigProp("trainingParams").replace("__USERID__", dev_id).replace("__BOTID__", aiid);
           int sCount = getWordCount(utils.getConfigProp("netroot").replace("__USERID__", dev_id).replace("__BOTID__", aiid)+"/"+"source.tok.txt.wordcount");
           int tCount = getWordCount(utils.getConfigProp("netroot").replace("__USERID__", dev_id).replace("__BOTID__", aiid) + "/" + "target.tok.txt.wordcount");
           script = script.replace("__SOURCE_COUNT__",""+(sCount+1)).replace("__SOURCE_COUNTPLUS__", "" + (sCount + 2));
           script = script.replace("__TARGET_COUNT__",""+(tCount+1)).replace("__TARGET_COUNTPLUS__", "" + (tCount + 2));

           script = script.replace("__TIMESTOP__", "" + dev.get_dev_plan_training_time(dev_id));
           ProcessBuilder pb = new ProcessBuilder( "python3.4",
                   utils.getConfigProp("trainingScript"),
                   utils.getConfigProp("netName"),
                   script,
                   "--botid",
                   aiid);
           Map<String, String> env = pb.environment();
           env.put("THEANO_FLAGS", "floatX=float32,device=gpu,nvcc.fastmath=True");
           env.put("PYTHONPATH", "/home/ubuntu/caffe/python:/usr/local/bin:/home/ubuntu/python/hutoma:/home/ubuntu/python/hutoma/neuralnetwork:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets/rnn:/home/ubuntu/python/hutoma/neuralnetwork/core:/home/ubuntu/python/hutoma/neuralnetwork/core/dataset:/home/ubuntu/python/hutoma/neuralnetwork/core/layers:/home/ubuntu/python/hutoma/neuralnetwork/core/models:/home/ubuntu/python/hutoma/neuralnetwork/core/trainer:/home/ubuntu/python/hutoma/neuralnetwork/core/utils");
           env.put("LD_LIBRARY_PATH", "/usr/local/cuda-6.5/lib64:/usr/local/cuda-6.5/lib64");
           env.put("PATH", "/usr/local/cuda-6.5/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games");
           pb.directory(new File(utils.getConfigProp("rnnroot")));
           File log = new File("/home/ubuntu/ai/"+dev_id+"/"+aiid+"log.txt");
           pb.redirectErrorStream(true);
           pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
           Process p = pb.start();
           ai.update_ai_training_status(aiid, String.valueOf(msg.training_in_progress));

       }
       catch (Exception e) {
           utils.debug("deep training exception:"+e.getMessage());
           System.err.println(e.getMessage());}
   }
    public static int startTraining (String dev_id, String aiid) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        int res=-1;
        prepTrainingFiles(dev_id, aiid);
        kick_off_training(dev_id, aiid);
        return res;
    }

    @POST
    @Path("/dl/{uid}/{aiid}/")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public void startRnnTraining( @Context SecurityContext securityContext,
                                  @DefaultValue("") @HeaderParam("_developer_id") String devid,
                                  @DefaultValue("start") @QueryParam("action") String action,
                                  @PathParam("uid") String uid,
                                  @PathParam("aiid") String aiid
                                  ) throws IOException, InterruptedException, SQLException, ClassNotFoundException {

        switch (action) {
            case "start":
                startTraining(uid, aiid);
                break;

            case "stop":
                hutoma.api.server.utils.localfs.copyFile(utils.getConfigProp("stopflag"), utils.getConfigProp("botdir") + uid + "/" + aiid + "/neuralnetwork/stopflag.txt");
                RNN.rnnQueueUpdate(uid, aiid, this._RNN_STOPPING);
                break;

        }


    }



    @POST
    @Path("/dl/{uid}/{aiid}/wakeup/")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public String RNN_wakeup(
                                @Context SecurityContext securityContext,
                                @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                @PathParam("aiid") String aiid,
                                @PathParam("uid") String uid) throws SQLException, ClassNotFoundException, IOException {

        api_root._status status = new api_root._status();
        status.code = 503;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        try {
            if (RNN.RNN_models_avaiable(dev_id, aiid)) return gson.toJson(status);
            ProcessBuilder pb = new ProcessBuilder(utils.getConfigProp("pythonPath"),
                    utils.getConfigProp("rnnServerPath"),
                    "--normalize",
                    "--beam-size",
                    "1",
                    "--beam-search",
                    "--state",
                    utils.getConfigProp("rnnServerParams1").replace("__USERID__", uid).replace("__BOTID__", aiid),
                    utils.getConfigProp("rnnServerParams2").replace("__USERID__", uid).replace("__BOTID__", aiid),
                    "--botid",
                    aiid,
                    "--keepalive",
                    utils.getConfigProp("keepalive")
            );
            Map<String, String> env = pb.environment();
            env.put("THEANO_FLAGS", "floatX=float32,device=gpu,nvcc.fastmath=True");
            env.put("PYTHONPATH", "/home/ubuntu/caffe/python:/usr/local/bin:/home/ubuntu/python/hutoma:/home/ubuntu/python/hutoma/neuralnetwork:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets:/home/ubuntu/python/hutoma/neuralnetwork/neuralnets/rnn:/home/ubuntu/python/hutoma/neuralnetwork/core:/home/ubuntu/python/hutoma/neuralnetwork/core/dataset:/home/ubuntu/python/hutoma/neuralnetwork/core/layers:/home/ubuntu/python/hutoma/neuralnetwork/core/models:/home/ubuntu/python/hutoma/neuralnetwork/core/trainer:/home/ubuntu/python/hutoma/neuralnetwork/core/utils");
            env.put("LD_LIBRARY_PATH", "/usr/local/cuda-6.5/lib64:/usr/local/cuda-6.5/lib64");
            env.put("PATH", "/usr/local/cuda-6.5/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games");
            pb.directory(new File(utils.getConfigProp("rnnroot")));


            File err = new File("/home/ubuntu/ai/"+uid+"/"+aiid+"/wakeup_errorlog.txt");
            File log = new File("/home/ubuntu/ai/"+uid+"/"+aiid+"/wakeup_log.txt");
            pb.redirectError(ProcessBuilder.Redirect.appendTo(err));
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            pb.redirectOutput();
            pb.redirectError();
            pb.start();
            status.code = 200;
        } catch (Exception ex) {
            utils.debug("wake up exception:"+ex.getMessage());
            status.code = 503;}
        return gson.toJson(status);
    }

}
