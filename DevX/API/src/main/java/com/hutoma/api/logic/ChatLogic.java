package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;

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

    public ApiResult chat(SecurityContext securityContext, String aiid, String dev_id, String q,
                          String uid, String history, boolean on_the_fly_learning, boolean fs, float min_p) {

        long timestampNow = tools.getTimestamp();

        ApiChat apiChat = new ApiChat(tools.createNewRandomUUID(), timestampNow);
        ChatResult res = new ChatResult();
        apiChat.setResult(res);

        res.setAction("no action");
        res.setContext("");
        res.setElapsedTime(0.0d);
        res.setQuery(q);

        long startTime = timestampNow;

        logger.logDebug(LOGFROM, "chat request for dev " + dev_id + " on ai " + aiid);
        boolean noResponse = true;
        try {
            ChatResult result = semanticAnalysis.getAnswer(dev_id, aiid, "[" +history+ "]" + q, min_p, fs);

            if (null!=result.getAnswer() && !result.getAnswer().isEmpty()) {
                noResponse = false;
                res.setScore(Math.round(Double.valueOf(result.getScore()) * 10.0d) / 10.0d);

                // take the second line in the answer, or an empty string if there isn't one
                res.setAnswer(result.getAnswer());

                long endWNetTime = tools.getTimestamp();
                res.setElapsedTime((endWNetTime - startTime)/1000.0d);

                apiChat.setTimestamp(endWNetTime);
                //apiChat.metadata = md;

                logger.logDebug(LOGFROM, "AI response in " + Long.toString(endWNetTime - startTime) + "ms with confidence " + Double.toString(res.getScore()));

                // if Semantic Analysis is not confident enough, try with the Neural Network
                if (res.getScore()<min_p)  {
                    logger.logDebug(LOGFROM, "starting RNN request");
                    String RNN_answer = neuralNet.getAnswer(dev_id, aiid, uid,q);
                    long endRNNTime = tools.getTimestamp();

                    boolean validRNN = false;
                    if ((RNN_answer!=null) && (!RNN_answer.isEmpty())) {
                        res.setAnswer(RNN_answer);
                        res.setElapsedTime((endRNNTime - startTime)/1000.0d);
                        validRNN = true;
                    }
                    logger.logDebug(LOGFROM, "RNN " + ((validRNN)? "valid":"*empty*") + " response in " +
                            Long.toString(endRNNTime - endWNetTime) + "ms. Total query time " +
                            Long.toString(endRNNTime - startTime) + "ms.");
                }
            }
        }
        catch (Exception ex){
            logger.logError(LOGFROM, "AI chat request exception: " + ex.toString());
            // log the error but don't return a 500
            // because the error may have occurred on the second request and the first may have completed correctly
        }
        if (noResponse) {
            logger.logWarning(LOGFROM, "no response from chat server");
            return ApiError.getInternalServerError();
        }

        return apiChat.setSuccessStatus();
    }
}



