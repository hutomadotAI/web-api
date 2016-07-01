package hutoma.api.server.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hutoma.api.server.AWS.msg;
import hutoma.api.server.Role;
import hutoma.api.server.Secured;
import hutoma.api.server.db.RNN;
import hutoma.api.server.dispatcher;
import hutoma.api.server.utils.utils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static hutoma.api.server.utils.utils.debug;

/**
 * Created by mauriziocibelli on 24/04/16.
 */


@Path("/ai/")
public class chat extends api_root {
    @Context
    SecurityContext securityContext;


    // Neural Network Query
    private String getRNNAnswer(String dev_id,String aiid,String uid,String q) throws InterruptedException, SQLException, ClassNotFoundException {
        String answer = "";

        // if the RNN network is not active, then push a message to get it activated
        if (!RNN.is_RNN_active(dev_id,aiid))
            hutoma.api.server.AWS.SQS.push_msg(utils.getConfigProp("core_queue"), msg.start_RNN + "|" + dev_id + "|" + aiid);

        long qid = hutoma.api.server.db.RNN.insertQuestion(dev_id,uid,aiid,q);
        if (qid<0) return  answer;
        int timeout = Integer.valueOf(utils.getConfigProp("RNNTimeout"));
        int counter = 0;


        // waits about 60 seconds for an answer
        while (counter <=timeout) {
            answer = hutoma.api.server.db.RNN.getAnswer(qid);
            if ((answer!=null) && (!answer.isEmpty())) break;
            counter ++;
            Thread.sleep(1000);
        }
        return answer;
    }


    @GET
    @Path("{aiid}/chat")
    @Secured({Role.ROLE_CLIENTONLY,Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String chat( @Context SecurityContext securityContext,
                        @PathParam("aiid") String aiid,
                        @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                        @DefaultValue("") @QueryParam("q") String q,
                        @DefaultValue("1") @QueryParam("uid") String uid,
//                      @DefaultValue("") @QueryParam("session_id") String session_id,
//                       @DefaultValue("") @QueryParam("contexts") String contexts,
//                       @DefaultValue("false") @QueryParam("resetContexts") boolean resetContexts,
//                       @DefaultValue("") @QueryParam("entities") String entities,
//                       @DefaultValue("") @QueryParam("location") String location,
//                       @DefaultValue("") @QueryParam("timezone") String timezone,
//                       @DefaultValue("") @QueryParam("ip") String ip,
//                       @DefaultValue("false") @QueryParam("store") boolean store,
                       @DefaultValue("false") @QueryParam("fs") boolean fs,
                       @DefaultValue("0.5") @QueryParam("min_p") float min_p
    ) throws IOException, InterruptedException {


        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        _chat response = new _chat();
        _result res = new _result();
        _metadata md = new _metadata();
        _status st = new _status();
        st.code = 200;
        st.info ="success";

        // Set Main response info
        response.id = UUID.randomUUID().toString();
        response.timestamp = new Timestamp(System.currentTimeMillis()).toString();
        response.result = res;
        response.metadata = md;
        response.status  = st;
        res.action ="no action";
        res.parameters ="no params";
        res.context = "";
        res.elapsed_time =0;
        res.query = q;

        long startTime = System.currentTimeMillis();
        try {

            String wnet_res = dispatcher.getAnswer(dev_id, aiid, q, min_p, fs);

            res.score = Double.valueOf(wnet_res.split("\\|")[0]);
            if (wnet_res!=null) {
                try{
                    res.answer = wnet_res.split("\\|")[1];
                }
                catch (Exception e) {
                    res.answer ="";}

                res.score = Math.round(res.score *10.0)/10.0;
                res.elapsed_time = System.currentTimeMillis() - startTime;
                response.timestamp = new Timestamp(System.currentTimeMillis()).toString();
                response.result = res;
                response.metadata = md;
                response.status = st;


                // if the AI is not confident enough, try with the RNN
                if (res.score<min_p)  {
                    String RNN_answer="";
                    RNN_answer = getRNNAnswer(dev_id,aiid,uid,q);
                    debug("RNN ANSWER IS:"+RNN_answer);
                    if ((RNN_answer!=null) && (!RNN_answer.isEmpty())) {
                        res.answer =RNN_answer;
                    }
                   }

            }
        }
        catch (Exception ex){
            res.answer = "";
            st.code = 500;
            st.info ="Error:AI not responding";

        }

        return gson.toJson(response);
    }


}



