package hutoma.api.server.ai;

import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.utils.utils;
import org.apache.commons.io.FileUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

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

        File f = new File(utils.getConfigProp("botdir") +   dev_id + "/" + aiid + "/binarized_text.target.shuff.h5");
        if (!f.exists()) {


         // NOT IMPLEMNTED YET

        }

    }

   private static void kick_off_training(String dev_id,String aiid) throws SQLException, ClassNotFoundException {
       try {

          //NOT IMPLEMTNED YET
       }
       catch (Exception e) {
           utils.debug("deecho ep training exception:"+e.getMessage());
           System.err.println(e.getMessage());}
   }

    public static int startTraining (String dev_id, String aiid) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        int res=-1;
        utils.debug("1");
        prepTrainingFiles(dev_id, aiid);
        utils.debug("2");
        kick_off_training(dev_id, aiid);
        return res;
    }



    @POST
    @Path("/dl/{uid}/{aiid}/")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
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
                hutoma.api.server.db.query.rnnQueueUpdate(uid, aiid, this._RNN_STOPPING);
                break;

        }


    }



}
