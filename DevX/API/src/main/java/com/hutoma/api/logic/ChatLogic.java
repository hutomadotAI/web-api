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
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by mauriziocibelli on 24/04/16.
 */
public class ChatLogic {

    private static final String LOGFROM = "chatlogic";
    private static final String HISTORY_REST_DIRECTIVE = "@reset";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final SemanticAnalysis semanticAnalysis;
    private final NeuralNet neuralNet;
    private final Tools tools;
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IEntityRecognizer entityRecognizer;


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

    public ApiResult chat(SecurityContext context, UUID aiid, String devId, String question, String chatId,
                          String history, String topic, float minP) {

        long timestampNow = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        ApiChat apiChat = new ApiChat(chatUuid, timestampNow);
        ChatResult chatResult = new ChatResult();
        apiChat.setResult(chatResult);
        apiChat.setID(chatUuid);

        long startTime = timestampNow;

        // Add telemetry for the request
        Map<String, String> telemetryMap = new HashMap<String, String>() {{
            put("DevId", devId);
            put("AIID", aiid.toString());
            put("Topic", topic);
            // TODO: potentially PII info, we may need to mask this later, but for
            // development purposes log this
            put("ChatId", chatUuid.toString());
            put("History", history);
            put("Q", question);
        }};

        boolean noResponse = true;
        boolean resetHistory = false;
        try {
            this.logger.logDebug(LOGFROM, "chat request for dev " + devId + " on ai " + aiid.toString());

            // async start both requests
            this.semanticAnalysis.startAnswerRequest(devId, aiid, chatUuid, topic, history, question, minP);
            this.neuralNet.startAnswerRequest(devId, aiid, chatUuid, question);

            // wait for semantic result to complete
            ChatResult semanticAnalysisResult = this.semanticAnalysis.getAnswerResult();

            // process result from semantic analysis
            if (null != semanticAnalysisResult.getAnswer()) {

                // if we receive a reset command then remove the command and flag the status
                if (semanticAnalysisResult.getAnswer().contains(HISTORY_REST_DIRECTIVE)) {
                    resetHistory = true;
                    semanticAnalysisResult.setAnswer(semanticAnalysisResult.getAnswer()
                            .replace(HISTORY_REST_DIRECTIVE, ""));
                }

                // remove trailing newline
                semanticAnalysisResult.setAnswer(semanticAnalysisResult.getAnswer().trim());

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

                this.logger.logDebug(LOGFROM, String.format("WNET response in %dms with confidence %f",
                        (endWNetTime - startTime), chatResult.getScore()));

                telemetryMap.put("WNETAnswer", chatResult.getAnswer());
                telemetryMap.put("WNETTopicOut", chatResult.getTopic_out());
                telemetryMap.put("WNETElapsedTime", Double.toString(chatResult.getElapsedTime()));

                // if semantic analysis is not confident enough, wait for and process result from neural network
                if ((semanticScore < minP) || (0.0d == semanticScore)) {

                    telemetryMap.put("WNETConfident", "false");

                    // wait for neural network to complete
                    String rnnAnswer = this.neuralNet.getAnswerResult(devId, aiid);
                    if (!rnnAnswer.isEmpty()) {
                        noResponse = false;
                    }
                    long endRNNTime = this.tools.getTimestamp();

                    boolean validRNN = false;
                    if (!rnnAnswer.isEmpty()) {
                        // rnn returns result in the form
                        // 0.157760821867|tell me then .
                        int splitIndex = rnnAnswer.indexOf('|');
                        if (splitIndex > 0) {
                            double neuralNetConfidence = Double.valueOf(rnnAnswer.substring(0, splitIndex));
                            chatResult.setAnswer(rnnAnswer.substring(splitIndex + 1).trim());
                            chatResult.setScore(toOneDecimalPlace(neuralNetConfidence));
                            chatResult.setElapsedTime((endRNNTime - startTime) / 1000.0d);
                            validRNN = true;
                        }
                    }
                    if (validRNN) {
                        this.logger.logDebug(LOGFROM, String.format("RNN response in %dms with confidence %f",
                                (endRNNTime - startTime), chatResult.getScore()));
                    } else {
                        this.logger.logDebug(LOGFROM, String.format("RNN invalid/empty response in %dms",
                                (endRNNTime - startTime)));
                    }

                    telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
                    telemetryMap.put("RNNValid", Boolean.toString(validRNN));
                    // TODO: potentially PII info
                    telemetryMap.put("RNNAnswer", chatResult.getAnswer());
                    telemetryMap.put("RNNTopicOut", chatResult.getTopic_out());
                }

                this.handleIntents(chatResult, devId, aiid, chatUuid, question, telemetryMap);

                // set the history to the answer, unless we have received a reset command, in which case send an empty string
                chatResult.setHistory(resetHistory ? "" : chatResult.getAnswer());
            }
        } catch (NeuralNet.NeuralNetAiNotFoundException notFoundException) {
            this.logger.logError(LOGFROM, "neural net not found");
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", notFoundException, telemetryMap);
            return ApiError.getNotFound("ai not found");
        } catch (NeuralNet.NeuralNetNotRespondingException nr) {
            this.logger.logError(LOGFROM, "neural net did not respond in time");
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", nr, telemetryMap);
            return ApiError.getNoResponse("unable to respond in time. try again");
        } catch (NeuralNet.NeuralNetRejectedAiStatusException rejected) {
            this.logger.logError(LOGFROM,
                    "question rejected because AI is in the wrong state: " + rejected.getMessage());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", rejected, telemetryMap);
            return ApiError.getBadRequest("This AI is not trained. Check the status and try again.");
        } catch (NeuralNet.NeuralNetException nne) {
            this.logger.logError(LOGFROM, "neural net exception: " + nne.toString());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", nne, telemetryMap);
            return ApiError.getInternalServerError();
        } catch (Exception ex) {
            this.logger.logError(LOGFROM, "AI chat request exception: " + ex.toString());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", ex, telemetryMap);
            // log the error but don't return a 500
            // because the error may have occurred on the second request and the first may have completed correctly
        }
        if (noResponse) {
            this.logger.logError(LOGFROM, "chat server returned an empty response");
            telemetryMap.put("EventType", "No response");
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", telemetryMap);
            return ApiError.getInternalServerError();
        }

        ITelemetry.addTelemetryEvent(this.logger, "ApiChat", telemetryMap);
        return apiChat.setSuccessStatus();
    }

    private double toOneDecimalPlace(double input) {
        return Math.round(input * 10.0d) / 10.0d;
    }

    /**
     * Handle any intents.
     * @param chatResult   the current chat result
     * @param aiid         the AI ID
     * @param chatUuid     the Chat ID
     * @param question     the question
     * @param telemetryMap the telemetry map
     */
    private void handleIntents(final ChatResult chatResult, final String devId, final UUID aiid, final UUID chatUuid,
                               final String question, final Map<String, String> telemetryMap) {
        // Now that have the chat result, we need to check if there's an intent being returned
        MemoryIntent memoryIntent = this.intentHandler.parseAiResponseForIntent(
                devId, aiid, chatUuid, chatResult.getAnswer());
        if (memoryIntent != null // Intent was recognized
                && !memoryIntent.isFulfilled()) {
            telemetryMap.put("IntentRecognized", memoryIntent.getName());
            if (memoryIntent.getUnfulfilledVariables().isEmpty()) {
                notifyIntentFulfilled(chatResult, memoryIntent, devId, aiid, telemetryMap);
            } else {
                // Attempt to retrieve entities from the question
                List<Pair<String, String>> entities = this.entityRecognizer.retrieveEntities(question,
                        memoryIntent.getVariables());
                if (!entities.isEmpty()) {
                    memoryIntent.fulfillVariables(entities);
                }

                // We've now fulfilled any variables present on the user's question.
                // Need to determine if there are still any unfulfilled variable
                // and prompt for it
                List<MemoryVariable> vars = memoryIntent.getUnfulfilledVariables();
                if (vars.isEmpty()) {
                    notifyIntentFulfilled(chatResult, memoryIntent, devId, aiid, telemetryMap);
                } else {
                    // For now get the first unfulfilled variable with numPrompts < maxPrompts
                    // or we could do random just to make it a 'surprise!' :)
                    Optional<MemoryVariable> optVariable = vars.stream()
                            .filter(x -> x.getTimesPrompted() < x.getTimesToPrompt()).findFirst();
                    if (optVariable.isPresent()) {
                        MemoryVariable variable = optVariable.get();
                        if (variable.getPrompts() == null || variable.getPrompts().isEmpty()) {
                            this.logger.logError(LOGFROM, "Variable with no prompts defined!");
                        } else {

                            // And prompt the user for the value for that variable
                            int pos = variable.getTimesPrompted() < variable.getPrompts().size()
                                    ? variable.getTimesPrompted()
                                    : 0;
                            chatResult.setAnswer(variable.getPrompts().get(pos));
                            // and decrement the number of prompts
                            variable.setTimesPrompted(variable.getTimesPrompted() + 1);
                            telemetryMap.put("IntentPrompt",
                                    String.format("intent:'%s' variable:'%s' currentPrompt:%d/%d",
                                            memoryIntent.getName(), variable.getName(),
                                            variable.getTimesPrompted(),
                                            variable.getTimesToPrompt()));

                        }
                    } else { // intent not fulfilled but no variables left to handle
                        // TODO: Currently we're not doing anything when the number of prompts is exceeded
                        // we just stop asking for that prompt, which means that the intent will remain
                        // unfulfilled after using all the prompts and the user may be left on their own
                        telemetryMap.put("IntentNotFulfilled", memoryIntent.getName());
                    }

                }
            }
            this.intentHandler.updateStatus(memoryIntent);
        }

        // Add the current intents state to the chat response
        chatResult.setIntents(this.intentHandler.getCurrentIntentsStateForChat(aiid, chatUuid));
    }

    private void notifyIntentFulfilled(ChatResult chatResult, MemoryIntent memoryIntent, String devId, UUID aiid,
                                       Map<String, String> telemetryMap) {
        memoryIntent.setIsFulfilled(true);
        ApiIntent intent = this.intentHandler.getIntent(devId, aiid, memoryIntent.getName());
        if (intent != null) {
            List<String> responses = intent.getResponses();
            chatResult.setAnswer(responses.get((int) (Math.random() * responses.size())));
        }
        telemetryMap.put("IntentFulfilled", memoryIntent.getName());

    }
}



