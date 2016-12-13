package com.hutoma.api.logic;

import com.hutoma.api.common.ChatTelemetryLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIChatServices;
import com.hutoma.api.connectors.ServerConnector;
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
    private final Tools tools;
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IEntityRecognizer entityRecognizer;
    private final AIChatServices chatServices;
    private final ChatTelemetryLogger chatTelemetryLogger;

    private Map<String, String> telemetryMap;

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, AIChatServices chatServices,
                     Tools tools, ILogger logger, IMemoryIntentHandler intentHandler,
                     IEntityRecognizer entityRecognizer, ChatTelemetryLogger chatTelemetryLogger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.intentHandler = intentHandler;
        this.entityRecognizer = entityRecognizer;
        this.chatTelemetryLogger = chatTelemetryLogger;
    }

    public ApiResult chat(SecurityContext context, UUID aiid, String devId, String question, String chatId,
                          String history, String topic, float minP) {

        // start timestamp
        long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, startTime);
        apiChat.setID(chatUuid);

        // Add telemetry for the request
        this.telemetryMap = new HashMap<String, String>() {
            {
                put("DevId", devId);
                put("AIID", aiid.toString());
                put("Topic", topic);
                // TODO: potentially PII info, we may need to mask this later, but for
                // development purposes log this
                put("ChatId", chatUuid.toString());
                put("History", history);
                put("Q", question);
            }
        };

        try {
            this.logger.logDebug(LOGFROM, "chat request for dev " + devId + " on ai " + aiid.toString());

            // async start requests to all servers
            this.chatServices.startChatRequests(devId, aiid, chatUuid, question, history, topic);

            // wait for WNET to return
            ChatResult result = this.interpretSemanticResult(startTime);

            // are we confident enough with this reply?
            boolean wnetConfident = (result.getScore() >= minP) && (result.getScore() > 0.0d);
            this.telemetryMap.put("WNETConfident", Boolean.toString(wnetConfident));

            if (wnetConfident) {
                // if we are taking WNET's reply then process intents
                this.handleIntents(result, devId, aiid, chatUuid, question, this.telemetryMap);
                this.telemetryMap.put("AnsweredBy", "WNET");
            } else {
                // otherwise,
                // wait for the AIML server to respond
                result = this.interpretAimlResult(startTime);

                // are we confident enough with this reply?
                boolean aimlConfident = (result.getScore() > 0.0d);
                this.telemetryMap.put("AIMLConfident", Boolean.toString(aimlConfident));

                if (aimlConfident) {
                    this.telemetryMap.put("AnsweredBy", "AIML");
                } else {
                    // get a response from the RNN
                    result = this.interpretRnnResult(startTime);
                    this.telemetryMap.put("AnsweredBy", "RNN");
                }
            }

            // set the history to the answer, unless we have received a reset command,
            // in which case send an empty string
            result.setHistory(result.isResetConversation() ? "" : result.getAnswer());

            // prepare to send back a result
            result.setScore(toOneDecimalPlace(result.getScore()));
            apiChat.setResult(result);

        } catch (AIChatServices.AiNotFoundException notFoundException) {
            this.logger.logError(LOGFROM, String.format("%s did not find ai %s", notFoundException.getMessage(), aiid));
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", notFoundException, this.telemetryMap);
            return ApiError.getNotFound("AI not found");

        } catch (AIChatServices.AiRejectedStatusException rejected) {
            this.logger.logError(LOGFROM,
                    "question rejected because AI is in the wrong state: " + rejected.getMessage());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", rejected, this.telemetryMap);
            return ApiError.getBadRequest("This AI is not trained. Check the status and try again.");

        } catch (ServerConnector.AiServicesException aiException) {
            this.logger.logError(LOGFROM, "AI services exception: " + aiException.toString());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", aiException, this.telemetryMap);
            return ApiError.getInternalServerError();

        } catch (IntentException ex) {
            this.logger.logError(LOGFROM, ex.toString());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", ex, this.telemetryMap);
            return ApiError.getInternalServerError();

        } catch (Exception e) {
            this.logger.logError(LOGFROM, "AI chat request exception: " + e.toString());
            ITelemetry.addTelemetryEvent(this.logger, "ApiChatError", e, this.telemetryMap);
            return ApiError.getInternalServerError();

        } finally {
            // once we've picked a result, abandon all the others to prevent hanging threads
            this.chatServices.abandonCalls();
        }
        // log the results
        ITelemetry.addTelemetryEvent(this.chatTelemetryLogger, "ApiChat", this.telemetryMap);
        return apiChat.setSuccessStatus();
    }

    private ChatResult interpretSemanticResult(long startTime) throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitWnet();

        // if we receive a reset command then remove the command and flag the status
        if (chatResult.getAnswer().contains(HISTORY_REST_DIRECTIVE)) {
            chatResult.setResetConversation(true);
            chatResult.setAnswer(chatResult.getAnswer()
                    .replace(HISTORY_REST_DIRECTIVE, ""));
        } else {
            chatResult.setResetConversation(false);
        }

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());
        chatResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.0d);

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("WNETAnswer", chatResult.getAnswer());
        this.telemetryMap.put("WNETTopicOut", chatResult.getTopic_out());
        this.telemetryMap.put("WNETElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretAimlResult(long startTime) throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitAiml();

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());
        chatResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.0d);

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("AIMLAnswer", chatResult.getAnswer());
        this.telemetryMap.put("AIMLElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretRnnResult(long startTime) throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitRnn();

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());
        chatResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.0d);

        this.logger.logDebug(LOGFROM, String.format("RNN response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
        this.telemetryMap.put("RNNAnswer", chatResult.getAnswer());
        this.telemetryMap.put("RNNTopicOut", chatResult.getTopic_out());

        return chatResult;
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
                               final String question, final Map<String, String> telemetryMap) throws IntentException {
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
                            // Should not happen as this should be validated during creation
                            this.logger.logError(LOGFROM, "Variable with no prompts defined!");
                            throw new IntentException(
                                    String.format("Entity %s for intent %s does not specify any prompts",
                                            memoryIntent.getName(), variable.getName()));
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

    static class IntentException extends Exception {
        public IntentException(final String message) {
            super(message);
        }
    }

}



