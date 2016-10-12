package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiChat;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    private static final String LOGFROM = "chatlogic";
    private Config config;
    private JsonSerializer jsonSerializer;
    private SemanticAnalysis semanticAnalysis;
    private NeuralNet neuralNet;
    private Tools tools;
    private ILogger logger;
    private IMemoryIntentHandler intentHandler;
    private IEntityRecognizer entityRecognizer;

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, SemanticAnalysis semanticAnalysis,
                     NeuralNet neuralNet, Tools tools, ILogger logger, IMemoryIntentHandler intentHandler,
                     IEntityRecognizer entityRecognizer) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.semanticAnalysis = semanticAnalysis;
        this.neuralNet = neuralNet;
        this.tools = tools;
        this.logger = logger;
        this.intentHandler = intentHandler;
        this.entityRecognizer = entityRecognizer;
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
            put("ChatId", chatUuid.toString());
            put("History", history);
            put("Q", q);
        }};

        boolean noResponse = true;
        try {
            this.logger.logDebug(LOGFROM, "chat request for dev " + dev_id + " on ai " + aiid.toString());

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

                logger.logDebug(LOGFROM, String.format("WNET response in %dms with confidence %f",
                        (endWNetTime - startTime), chatResult.getScore()));

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
                    if (!RNN_answer.isEmpty()) {
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
                        logger.logDebug(LOGFROM, String.format("RNN response in %dms with confidence %f",
                                (endRNNTime - startTime), chatResult.getScore()));
                    } else {
                        logger.logDebug(LOGFROM, String.format("RNN invalid/empty response in %dms",
                                (endRNNTime - startTime)));
                    }

                    telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
                    telemetryMap.put("RNNValid", Boolean.toString(validRNN));
                    // TODO: potentially PII info
                    telemetryMap.put("RNNAnswer", chatResult.getAnswer());
                    telemetryMap.put("RNNTopicOut", chatResult.getTopic_out());
                }

                this.handleIntents(chatResult, dev_id, aiid, chatUuid, q, telemetryMap);
            }
        } catch (NeuralNet.NeuralNetNotRespondingException nr) {
            this.logger.logError(LOGFROM, "neural net did not respond in time");
            this.addTelemetry("ApiChatError", nr, telemetryMap);
            return ApiError.getNoResponse("unable to respond in time. try again");
        } catch (NeuralNet.NeuralNetException nne) {
            this.logger.logError(LOGFROM, "neural net exception: " + nne.toString());
            this.addTelemetry("ApiChatError", nne, telemetryMap);
            return ApiError.getInternalServerError();
        } catch (Exception ex) {
            this.logger.logError(LOGFROM, "AI chat request exception: " + ex.toString());
            this.addTelemetry("ApiChatError", ex, telemetryMap);
            // log the error but don't return a 500
            // because the error may have occurred on the second request and the first may have completed correctly
        }
        if (noResponse) {
            this.logger.logError(LOGFROM, "chat server returned an empty response");
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

    /**
     * Handle any intents.
     * @param chatResult the current chat result
     * @param aiid the AI ID
     * @param chatUuid the Chat ID
     * @param question the question
     * @param telemetryMap the telemetry map
     */
    private void handleIntents(final ChatResult chatResult, final String devId, final UUID aiid, final UUID chatUuid,
                               final String question, final Map<String, String> telemetryMap) {
        // Now that have the chat result, we need to check if there's an intent being returned
        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                devId, aiid, chatUuid, chatResult.getAnswer());
        if (memoryIntent != null) { // Intent was recognized
            telemetryMap.put("IntentRecognized", memoryIntent.getName());
            if (memoryIntent.getUnfulfilledVariables().isEmpty()) {
                memoryIntent.setIsFulfilled(true);
                telemetryMap.put("IntentFulfilled", memoryIntent.getName());
            } else {
                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = entityRecognizer.retrieveEntities(question,
                        memoryIntent.getVariables());
                if (!entities.isEmpty()) {
                    memoryIntent.fulfillVariables(entities);
                }

                // We've now fulfilled any variables present on the user's question.
                // Need to determine if there are still any unfulfilled variable
                // and prompt for it
                List<MemoryVariable> vars = memoryIntent.getUnfulfilledVariables();
                if (vars.isEmpty()) {
                    memoryIntent.setIsFulfilled(true);
                    telemetryMap.put("IntentFulfilled", memoryIntent.getName());
                } else {
                    // For now get the first unfulfilled variable with numPrompts>0
                    // or we could do random just to make it a 'surprise!' :)
                    Optional<MemoryVariable> optVariable = vars.stream()
                            .filter(x -> x.getTimesToPrompt() > 0).findFirst();
                    if (optVariable.isPresent()) {
                        MemoryVariable variable = optVariable.get();
                        if (variable.getPrompts() == null || variable.getPrompts().isEmpty()) {
                            logger.logError(LOGFROM, "Variable with no prompts defined!");
                        } else {
                            // And prompt the user for the value for that variable
                            int pos = variable.getPrompts().size() >= variable.getTimesToPrompt()
                                    ? variable.getPrompts().size() - variable.getTimesToPrompt()
                                    : 0;
                            chatResult.setAnswer(variable.getPrompts().get(pos));
                            // and decrement the number of prompts
                            variable.setTimesPrompted(variable.getTimesToPrompt() - 1);
                            telemetryMap.put("IntentPrompt",
                                    String.format("intent:'%s' variable:'%s' remainingPrompts:%d",
                                            memoryIntent.getName(), variable.getName(), variable.getTimesToPrompt()));

                        }
                    } else { // intent not fulfilled but no variables left to handle
                        // TODO: Currently we're not doing anything when the number of prompts is exceeded
                        // we just stop asking for that prompt, which means that the intent will remain
                        // unfulfilled after using all the prompts and the user may be left on their own
                        telemetryMap.put("IntentNotFulfilled", memoryIntent.getName());
                    }

                }
            }
            intentHandler.updateStatus(memoryIntent);
        }

        // Add the current intents state to the chat response
        chatResult.setIntents(intentHandler.getCurrentIntentsStateForChat(aiid, chatUuid));
    }
}



