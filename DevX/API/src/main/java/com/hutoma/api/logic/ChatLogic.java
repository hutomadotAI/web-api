package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.validation.Validate;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

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

    private double toOneDecimalPlace(double input) {
        return Math.round(input * 10.0d) / 10.0d;
    }

    public ApiResult chat(SecurityContext context, UUID aiid, String dev_id, String q, String uid, String history,
                          String topic, float min_p) {

        long timestampNow = tools.getTimestamp();
        UUID chatID = tools.createNewRandomUUID();

        ApiChat apiChat = new ApiChat(tools.createNewRandomUUID(), timestampNow);
        ChatResult chatResult = new ChatResult();
        apiChat.setResult(chatResult);
        apiChat.setID(chatID);

        long startTime = timestampNow;

        boolean noResponse = true;
        try {
            logger.logDebug(LOGFROM, "chat request for dev " + dev_id + " on ai " + aiid.toString());

            // async start both requests
            semanticAnalysis.startAnswerRequest(dev_id, aiid, uid, topic, history, q, min_p);
            neuralNet.startAnswerRequest(dev_id, aiid, uid, q);

            // wait for semantic result to complete
            ChatResult semanticAnalysisResult = semanticAnalysis.getAnswerResult();

            // process result from semantic analysis
            if (null!=semanticAnalysisResult.getAnswer()) {
                if (!semanticAnalysisResult.getAnswer().isEmpty()) {
                    noResponse = false;
                }

                double semanticScore = semanticAnalysisResult.getScore();
                chatResult.setScore(toOneDecimalPlace(semanticScore));
                chatResult.setTopic_out(semanticAnalysisResult.getTopic_out());
                chatResult.setAnswer(semanticAnalysisResult.getAnswer());

                long endWNetTime = tools.getTimestamp();
                chatResult.setElapsedTime((endWNetTime - startTime)/1000.0d);

                apiChat.setTimestamp(endWNetTime);

                logger.logDebug(LOGFROM, "WNET response in " + Long.toString(endWNetTime - startTime) + "ms with confidence " + Double.toString(chatResult.getScore()));

                // if semantic analysis is not confident enough, wait for and process result from neural network
                if ((semanticScore < min_p) || (0.0d == semanticScore))  {

                    // wait for neural network to complete
                    String RNN_answer = neuralNet.getAnswerResult();
                    if (!RNN_answer.isEmpty()) {
                        noResponse = false;
                    }
                    long endRNNTime = tools.getTimestamp();

                    boolean validRNN = false;
                    if ((RNN_answer!=null) && (!RNN_answer.isEmpty())) {

                        // rnn returns result in the form
                        // 0.157760821867|tell me then .
                        int splitIndex = RNN_answer.indexOf('|');
                        if (splitIndex>0) {
                            double neuralNetConfidence = Double.valueOf(RNN_answer.substring(0, splitIndex));
                            chatResult.setAnswer(RNN_answer.substring(splitIndex+1));
                            chatResult.setScore(toOneDecimalPlace(neuralNetConfidence));
                            chatResult.setElapsedTime((endRNNTime - startTime)/1000.0d);
                            validRNN = true;
                        }
                    }
                    if (validRNN) {
                        logger.logDebug(LOGFROM, "RNN response in " + Long.toString(endRNNTime - startTime) + "ms with confidence " + Double.toString(chatResult.getScore()));
                    } else {
                        logger.logDebug(LOGFROM, "RNN invalid/empty response in " + Long.toString(endRNNTime - startTime) + "ms.");
                    }
                }
            }
        }
        catch (NeuralNet.NeuralNetNotRespondingException nr) {
            logger.logError(LOGFROM, "neural net did not respond in time");
            return ApiError.getNoResponse("unable to respond in time. try again");
        }
        catch (NeuralNet.NeuralNetException nne) {
            logger.logError(LOGFROM, "neural net exception: " + nne.toString());
            return ApiError.getInternalServerError();
        }
        catch (Exception ex){
            logger.logError(LOGFROM, "AI chat request exception: " + ex.toString());
            // log the error but don't return a 500
            // because the error may have occurred on the second request and the first may have completed correctly
        }
        if (noResponse) {
            logger.logError(LOGFROM, "chat server returned an empty response");
            return ApiError.getInternalServerError();
        }

        return apiChat.setSuccessStatus();
    }

}



