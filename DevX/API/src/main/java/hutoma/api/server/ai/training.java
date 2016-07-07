package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.db.ai;
import hutoma.api.server.utils.utils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static hutoma.api.server.utils.utils.getConfigProp;

/**
 * Created by mauriziocibelli on 28/04/16.
 */
@Path("/ai/")
public class training {



    // Parse the training file to check for errors and to build
    // the conversation history structure

    private String parseTrainingFile(ArrayList<String> training) {
        String parsedFile="";
        String currentSentence ="";


        String previousSentence = "";
        int ConversationCounter = 0;

        try {
            for (String s:training) {
                currentSentence = s;

                // reset chat history
                if (s.length()==0) {
                    ConversationCounter = 0;
                    previousSentence = "";
                }
                ConversationCounter ++;

                // check if the conversation is longer than just answer and question
                // if yes, and if the current sentence is a question, add the previous sentence
                if ((ConversationCounter > 2) && (ConversationCounter & 1) != 0)
                    currentSentence = "CMDHSTART "+ previousSentence +" CMDHEND " + currentSentence;
                else previousSentence = currentSentence;

                parsedFile = currentSentence+"\n";

            }
        }
        catch (Exception ex) {parsedFile="";}

     return  parsedFile;
    }



    @POST
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile( @Context SecurityContext securityContext,
                              @DefaultValue("") @HeaderParam("_developer_id") String devid,
                              @PathParam("aiid") String aiid,
                              @FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<String> source = new ArrayList<>();

        api_root._myAIs myai = new api_root._myAIs();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        myai.status =st;
        api_root._ai _ai = new api_root._ai();

        try {
            String trainingFile="";
            int n_lines = 0;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    source.add(line);
                    trainingFile += line + "\n";
                    n_lines++;
                }
                reader.close();
            }
            catch (Exception ex) {}

            ai.update_ai_training_file(aiid, trainingFile);
            hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"),msg.ready_for_training+"|"+devid+"|"+aiid);

            int max_cluster_lines = 5000;
            double cluster_min_probability = 0.7;

            try {
                max_cluster_lines = Integer.valueOf(getConfigProp("max_cluster_lines"));
                cluster_min_probability = Double.valueOf(getConfigProp("cluster_min_probability"));
            }
            catch (Exception ex) {}


            if (n_lines>max_cluster_lines) {
                hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"),msg.cluster_split + "|" + devid + "|" + aiid +"|"+cluster_min_probability);
            }

        }
        catch (Exception ex) {
                st.code = 500;
                st.info = "Error:Internal Server Error.";
            }

        finally {
            try { uploadedInputStream.close(); } catch (Throwable ignore) {}
         }
        return gson.toJson(myai);
    }


    @DELETE
    @Path("/{aiid}/training")
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String delete( @Context SecurityContext securityContext,
                              @DefaultValue("") @HeaderParam("_developer_id") String devid,
                              @PathParam("aiid") String aiid) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<String> source = new ArrayList<>();
        ArrayList<String> target = new ArrayList<>();
        api_root._myAIs myai = new api_root._myAIs();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";
        myai.status =st;
        api_root._ai _ai = new api_root._ai();
        hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"),msg.delete_training+"|"+devid+"|"+aiid);
        _ai.ai_status =0;
        return gson.toJson(myai);
    }



    @Path("/{aiid}/training")
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getsingle_ai(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @PathParam("aiid") String aiid) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        api_root._status st = new api_root._status();
        api_root._ai _myai = new api_root._ai();
        st.code = 200;
        st.info ="success";

        try {
            _myai = ai.get_ai(aiid);

        } catch (Exception e){
            st.code = 500;
            st.info = "Error:Internal Server Error.";
        }

        return gson.toJson(_myai);
    }

}
