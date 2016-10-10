package com.hutoma.api.logic;

import com.hutoma.api.common.*;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    private final String LOGFROM = "chatlogic";
    Config config;
    JsonSerializer jsonSerializer;
    SemanticAnalysis semanticAnalysis;
    NeuralNet neuralNet;
    Tools tools;
    ILogger logger;

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, SemanticAnalysis semanticAnalysis, NeuralNet neuralNet, Tools tools, ILogger logger) {
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

    public ApiResult chat(SecurityContext context, UUID aiid, String dev_id, String q, String chatId, String history,
                          String topic, float min_p) {

        long timestampNow = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        ApiChat apiChat = new ApiChat(chatUuid, timestampNow);
        ChatResult chatResult = new ChatResult();
        apiChat.setResult(chatResult);
        apiChat.setID(chatUuid);

        long startTime = timestampNow;

        // Add telemetry for the request
        Map<String, String> telemetryMap = new HashMap<String, String>() {{
            put("DevId", dev_id);
            put("AIID", aiid.toString());
            put("Topic", topic);
            // TODO: potentially PII info, we may need to mask this later, but for
            // development purposes log this
            put("UID", chatUuid.toString());
            put("History", history);
            put("Q", q);
        }};

        boolean noResponse = true;
        try {
            this.logger.logDebug(this.LOGFROM, "chat request for dev " + dev_id + " on ai " + aiid.toString());

            // async start both requests
            this.semanticAnalysis.startAnswerRequest(dev_id, aiid, chatUuid, topic, history, q, min_p);
            this.neuralNet.startAnswerRequest(dev_id, aiid, chatUuid, q);

            // wait for semantic result to complete
            ChatResult semanticAnalysisResult = this.semanticAnalysis.getAnswerResult();

            // process result from semantic analysis
            if (null != semanticAnalysisResult.getAnswer()) {
                if (!semanticAnalysisResult.getAnswer().isEmpty()) {
                    noResponse = false;
                }

                double semanticScore = semanticAnalysisResult.getScore();
                chatResult.setChatId(chatUuid);
                chatResult.setScore(toOneDecimalPlace(semanticScore));
                chatResult.setTopic_out(semanticAnalysisResult.getTopic_out());
                chatResult.setAnswer(semanticAnalysisResult.getAnswer());

                long endWNetTime = this.tools.getTimestamp();
                chatResult.setElapsedTime((endWNetTime - startTime) / 1000.0d);

                apiChat.setTimestamp(endWNetTime);

                this.logger.logDebug(this.LOGFROM, "WNET response in " + Long.toString(endWNetTime - startTime) + "ms with confidence " + Double.toString(chatResult.getScore()));
                telemetryMap.put("WNETAnswer", chatResult.getAnswer());
                telemetryMap.put("WNETTopicOut", chatResult.getTopic_out());
                telemetryMap.put("WNETElapsedTime", Double.toString(chatResult.getElapsedTime()));

                // if semantic analysis is not confident enough, wait for and process result from neural network
                if ((semanticScore < min_p) || (0.0d == semanticScore)) {

                    telemetryMap.put("WNETConfident", "false");

                    // wait for neural network to complete
                    String RNN_answer = this.neuralNet.getAnswerResult();
                    if (!RNN_answer.isEmpty()) {
                        noResponse = false;
                    }
                    long endRNNTime = this.tools.getTimestamp();

                    boolean validRNN = false;
                    if ((RNN_answer != null) && (!RNN_answer.isEmpty())) {

                        // rnn returns result in the form
                        // 0.157760821867|tell me then .
                        int splitIndex = RNN_answer.indexOf('|');
                        if (splitIndex > 0) {
                            double neuralNetConfidence = Double.valueOf(RNN_answer.substring(0, splitIndex));
                            chatResult.setAnswer(RNN_answer.substring(splitIndex + 1));
                            chatResult.setScore(toOneDecimalPlace(neuralNetConfidence));
                            chatResult.setElapsedTime((endRNNTime - startTime) / 1000.0d);
                            validRNN = true;
                        }
                    }
                    if (validRNN) {
                        this.logger.logDebug(this.LOGFROM, "RNN response in " + Long.toString(endRNNTime - startTime) + "ms with confidence " + Double.toString(chatResult.getScore()));
                    } else {
                        this.logger.logDebug(this.LOGFROM, "RNN invalid/empty response in " + Long.toString(endRNNTime - startTime) + "ms.");
                    }

                    telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
                    telemetryMap.put("RNNValid", Boolean.toString(validRNN));
                    // TODO: potentially PII info
                    telemetryMap.put("RNNAnswer", chatResult.getAnswer());
                    telemetryMap.put("RNNTopicOut", chatResult.getTopic_out());
                }
            }
        } catch (NeuralNet.NeuralNetNotRespondingException nr) {
            this.logger.logError(this.LOGFROM, "neural net did not respond in time");
            this.addTelemetry("ApiChatError", nr, telemetryMap);
            return ApiError.getNoResponse("unable to respond in time. try again");
        } catch (NeuralNet.NeuralNetException nne) {
            this.logger.logError(this.LOGFROM, "neural net exception: " + nne.toString());
            this.addTelemetry("ApiChatError", nne, telemetryMap);
            return ApiError.getInternalServerError();
        } catch (Exception ex) {
            this.logger.logError(this.LOGFROM, "AI chat request exception: " + ex.toString());
            this.addTelemetry("ApiChatError", ex, telemetryMap);
            // log the error but don't return a 500
            // because the error may have occurred on the second request and the first may have completed correctly
        }
        if (noResponse) {
            this.logger.logError(this.LOGFROM, "chat server returned an empty response");
            telemetryMap.put("EventType", "No response");
            this.addTelemetry("ApiChatError", telemetryMap);
            return ApiError.getInternalServerError();
        }

        this.addTelemetry("ApiChat", telemetryMap);
        return apiChat.setSuccessStatus();
    }

    private void addTelemetry(String eventName, Exception ex, Map<String, String> parameters) {
        if (this.logger instanceof ITelemetry) {
            ((ITelemetry) this.logger).addTelemetryEvent(eventName, ex, parameters);
        }
    }

    private void addTelemetry(String eventName, Map<String, String> parameters) {
        if (this.logger instanceof ITelemetry) {
            ((ITelemetry) this.logger).addTelemetryEvent(eventName, parameters);
        }
    }
}



