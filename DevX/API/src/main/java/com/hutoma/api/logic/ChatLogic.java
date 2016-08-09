package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import hutoma.api.server.ai.api_root;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    Config config;
    JsonSerializer jsonSerializer;
    SemanticAnalysis semanticAnalysis;
    NeuralNet neuralNet;
    Tools tools;
    Logger logger;

    private final String LOGFROM = "chatlogic";

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, SemanticAnalysis semanticAnalysis, NeuralNet neuralNet, Tools tools, Logger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.semanticAnalysis = semanticAnalysis;
        this.neuralNet = neuralNet;
        this.tools = tools;
        this.logger = logger;
    }

    public String chat(SecurityContext securityContext, String aiid, String dev_id, String q,
                       String uid, String history, boolean on_the_fly_learning, boolean fs, float min_p) {

        api_root._chat response = new api_root._chat();
        api_root._result res = new api_root._result();
        api_root._metadata md = new api_root._metadata();
        api_root._status st = new api_root._status();
        st.code = 200;
        st.info ="success";

        long timestampNow = tools.getTimestamp();

        // Set Main response info
        response.id = tools.createNewRandomUUID().toString();
        response.timestamp = timestampNow;
        response.result = res;
        response.metadata = md;
        response.status  = st;
        res.action ="no action";
        //res.parameters ="no params";
        res.context = "";
        res.elapsed_time =0;
        res.query = q;

        long startTime = timestampNow;

        logger.logDebug(LOGFROM, "chat request for dev " + dev_id + " on ai " + aiid);
        boolean noResponse = true;
        try {
            String wnet_res = semanticAnalysis.getAnswer(dev_id, aiid, "[" +history+ "]" + q, min_p, fs);
            if (null!=wnet_res) {
                String[] answers = wnet_res.split("\\|");
                if (answers.length>1) {
                    noResponse = false;
                    res.score = Math.round(Double.valueOf(answers[0]) * 10.0d) / 10.0d;

                    // take the second line in the answer, or an empty string if there isn't one
                    res.answer = (answers.length>1)? answers[1]:"";

                    long endWNetTime = tools.getTimestamp();
                    res.elapsed_time = endWNetTime - startTime;

                    response.timestamp = endWNetTime;
                    response.result = res;
                    response.metadata = md;
                    response.status = st;
                    logger.logDebug(LOGFROM, "AI response in " + Long.toString(endWNetTime - startTime) + "ms with confidence " + Double.toString(res.score));

                    // if Semantic Analysis is not confident enough, try with the Neural Network
                    if (res.score<min_p)  {
                        logger.logDebug(LOGFROM, "starting RNN request");
                        String RNN_answer = neuralNet.getAnswer(dev_id, aiid, uid,q);
                        long endRNNTime = tools.getTimestamp();

                        boolean validRNN = false;
                        if ((RNN_answer!=null) && (!RNN_answer.isEmpty())) {
                            res.answer = RNN_answer;
                            validRNN = true;
                        }
                        logger.logDebug(LOGFROM, "RNN " + ((validRNN)? "valid":"*empty*") + " response in " +
                                Long.toString(endRNNTime - endWNetTime) + "ms. Total query time " +
                                Long.toString(endRNNTime - startTime) + "ms.");
                    }
                }
            }
        }
        catch (Exception ex){
            logger.logError(LOGFROM, "AI chat request exception" + ex.toString());
        }
        if (noResponse) {
            logger.logWarning(LOGFROM, "no response from chat server");
            res.answer = "";
            st.code = 500;
            st.info ="Error:AI not responding";
        }

        return jsonSerializer.serialize(response);
    }
}



