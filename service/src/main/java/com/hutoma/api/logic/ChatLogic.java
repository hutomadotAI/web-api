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
import com.hutoma.api.containers.AssistantSessions;
import com.hutoma.api.containers.AssistantState;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.memory.IEntityRecognizer;
import com.hutoma.api.memory.IMemoryIntentHandler;

import org.mortbay.util.ajax.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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

    // @TODO demo hack
    private final AssistantSessions assistantSessions;
    private String chatId;
    // @TODO /demo hack

    @Inject
    public ChatLogic(Config config, JsonSerializer jsonSerializer, AIChatServices chatServices,
                     Tools tools, ILogger logger, IMemoryIntentHandler intentHandler,
                     IEntityRecognizer entityRecognizer, ChatTelemetryLogger chatTelemetryLogger, AssistantSessions sessions) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.chatServices = chatServices;
        this.tools = tools;
        this.logger = logger;
        this.intentHandler = intentHandler;
        this.entityRecognizer = entityRecognizer;
        this.chatTelemetryLogger = chatTelemetryLogger;

        // @TODO demo hack
        this.assistantSessions = sessions;
        // @TODO /demo hack
    }

    public ApiResult chat(SecurityContext context, UUID aiid, String devId, String question, String chatId,
                          String history, String topic, float minP) {

        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);

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
            ChatResult result = this.interpretSemanticResult();

            // are we confident enough with this reply?
            boolean wnetConfident = (result.getScore() >= minP) && (result.getScore() > 0.0d);
            this.telemetryMap.put("WNETConfident", Boolean.toString(wnetConfident));

            if (wnetConfident) {
                // if we are taking WNET's reply then process intents
                if (this.handleIntents(result, devId, aiid, chatUuid, question, this.telemetryMap)) {
                    this.telemetryMap.put("AnsweredBy", "WNET");
                    this.telemetryMap.put("AnsweredWithConfidence", "true");
                } else {
                    // if intents processing returns false then we need to ignore WNET
                    wnetConfident = false;
                }
            }

            if (!wnetConfident) {
                // otherwise,
                // wait for the AIML server to respond
                result = this.interpretAimlResult();

                // are we confident enough with this reply?
                boolean aimlConfident = (result.getScore() > 0.0d);
                this.telemetryMap.put("AIMLConfident", Boolean.toString(aimlConfident));

                if (aimlConfident) {
                    this.telemetryMap.put("AnsweredBy", "AIML");
                    this.telemetryMap.put("AnsweredWithConfidence", "true");
                } else {
                    // get a response from the RNN
                    ChatResult rnnResult = this.interpretRnnResult();

                    // If the RNN was clueless or returned an empty response
                    if (rnnResult.getAnswer() == null || rnnResult.getAnswer().isEmpty()) {
                        // Use AIML's smartmouth response as it will always generate something
                        this.telemetryMap.put("AnsweredBy", "AIML");
                        // Mark it as not really answered
                        this.telemetryMap.put("AnsweredWithConfidence", "false");
                    } else {
                        result = rnnResult;
                        this.telemetryMap.put("AnsweredBy", "RNN");
                        this.telemetryMap.put("AnsweredWithConfidence", "true");
                    }
                }
            }

            // set the history to the answer, unless we have received a reset command,
            // in which case send an empty string
            result.setHistory(result.isResetConversation() ? "" : result.getAnswer());

            // prepare to send back a result
            result.setScore(toOneDecimalPlace(result.getScore()));

            // set the chat response time to the whole duration since the start of the request until now
            result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);
            this.telemetryMap.put("RequestDuration", Double.toString(result.getElapsedTime()));

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
            this.logger.logException(LOGFROM, e);
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

    public ApiResult assistantChat(SecurityContext context, UUID aiid, String devId, String question, String chatId,
                                   String history, String topic, float minP) {

        final long startTime = this.tools.getTimestamp();
        UUID chatUuid = UUID.fromString(chatId);
        this.chatId = chatId;

        if (!assistantSessions.sessions.containsKey(chatId)) {
            assistantSessions.sessions.put(chatId, new AssistantState());
        }

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

        String answer = null;

        try {
            answer = insertMessage(question);
        }
        catch (Exception ex) {
            this.logger.logError(LOGFROM, ex.toString());
            ITelemetry.addTelemetryEvent(this.logger, "AssistantChatError", ex, this.telemetryMap);
            return ApiError.getInternalServerError(ex.getMessage());
        }

        ChatResult result = new ChatResult();
        result.setElapsedTime(this.tools.getTimestamp() - startTime);
        result.setQuery(question);
        result.setHistory(answer);

        // Set a fixed response.
        result.setAnswer(answer);
        result.setScore(1.0);

        // set the chat response time to the whole duration since the start of the request until now
        result.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);

        this.telemetryMap.put("RequestDuration", Double.toString(result.getElapsedTime()));

        // prepare the result container
        ApiChat apiChat = new ApiChat(chatUuid, 0);
        apiChat.setResult(result);
        sessionData(chatId).setHist(answer);

        return apiChat.setSuccessStatus();
    }

    // @TODO demo hack
    private void setAlarmId(String message) {
        message = message.toLowerCase();

        if  (message.contains("7657")) {
            sessionData(chatId).setAlarmId(7657);
            sessionData(chatId).setActionId(1);
        }
        if  (message.contains("7650")) {
            sessionData(chatId).setAlarmId(7650);
            sessionData(chatId).setActionId(1);
        }
        if  (message.contains("2683")) {
            sessionData(chatId).setAlarmId(2683);
            sessionData(chatId).setActionId(1);
        }
        if  (message.contains("lte68")) {
            sessionData(chatId).setAlarmId(68);
            sessionData(chatId).setActionId(1);
        }
        if (message.contains("70359")
                || message.contains("fail") && message.contains("hdd")
                || message.contains("disk") && message.contains("fail")
                || message.contains("problem") && message.contains("hdd")
                || message.contains("disk") && message.contains("problem")
                || message.contains("issue") && message.contains("hdd")
                || message.contains("disk") && message.contains("issue")
                || message.contains("disk") && message.contains("alarm")
                || message.contains("hdd") && message.contains("alarm")) {
            sessionData(chatId).setAlarmId(70359);
        }

        if (message.contains("3159") || message.contains("3199")
                || message.contains("temp") && message.contains("high")
                || message.contains("temperature") && message.contains("high")
                || message.contains("subrack") && message.contains("hot")
                || message.contains("temp") && message.contains("high")
                || message.contains("disk") && message.contains("issue")
                || message.contains("temperature") && message.contains("issue")
                || message.contains("temperature") && message.contains("alarm")) {
            sessionData(chatId).setAlarmId(3199);
            sessionData(chatId).setActionId(1);
        }
    }

    public void setAction(String message) {
        message = message.toLowerCase();

        if (message.contains("troubleshoot")
                || message.contains("debug")
                || message.contains("fix")
                || message.contains("clear")) {
            sessionData(chatId).setActionId(1);
        }

        if (message.contains("read")
                || message.contains("description")
                || message.contains("describe")) {
            sessionData(chatId).setAlarmId(2);
        }
    }

    private void resetMemory() {
        sessionData(chatId).setCurrentAI(1);
        sessionData(chatId).setHist("");
        sessionData(chatId).setT("");
        sessionData(chatId).setActionId(-1);
        sessionData(chatId).setAlarmId(-1);
        sessionData(chatId).setTopic(-1);
        sessionData(chatId).setFlag(0);
        sessionData(chatId).setNtries(0);
    }

    private String insertMessage(String message) throws IOException {
        List<String> results = new ArrayList<String>();
        if (message == "") {
            return "";
        }

        // setAction(message);
        setAlarmId(message);

        if (sessionData(chatId).getActionId() > 0 || sessionData(chatId).getAlarmId() > 0) {
            sessionData(chatId).setCurrentAI(0);
        }
        else {
            sessionData(chatId).setCurrentAI(1);
        }

        if (message.toLowerCase().contains("new issue") || message.toLowerCase().contains("reset")
                || message.toLowerCase().contains("new topic") || message.toLowerCase().contains("forget")) {
            resetMemory();
            results.add("Ok, let's start fresh!");
            message = "";
        }

        if (sessionData(chatId).getActionId() < 0 && sessionData(chatId).getAlarmId() < 0) {
            sessionData(chatId).setCurrentAI(1);
        }
        if (sessionData(chatId).getActionId() > 0 && sessionData(chatId).getAlarmId() < 0) {
            sessionData(chatId).setNtries(sessionData(chatId).getNtries() + 1);
            if (sessionData(chatId).getNtries() == 1){
                results.add("Can you please tell me the alarm number if you are refering to? You can type 70359 or 3159.");
            }
            else {
                sessionData(chatId).setCurrentAI(1);
                resetMemory();
            }
        }
        if (sessionData(chatId).getActionId() < 0 && sessionData(chatId).getAlarmId() > 0) {
            sessionData(chatId).setNtries(sessionData(chatId).getNtries() + 1);
            if (sessionData(chatId).getNtries() == 1) {
                results.add("Do you want to troubleshoot the alarm or check the description?");
            }
            else {
                sessionData(chatId).setCurrentAI(1);
                resetMemory();
            }
        }

        if (sessionData(chatId).getActionId() > 0 && sessionData(chatId).getAlarmId() > 0) {
            sessionData(chatId).setTopic(sessionData(chatId).getAlarmId());
            sessionData(chatId).setT(Integer.toString(sessionData(chatId).getAlarmId()));

            if ((sessionData(chatId).getAlarmId() == 7657) && (sessionData(chatId).getFlag() == 0)) {
                sessionData(chatId).setFlag(1);
                message = "alarm 7657 troubleshoot";
            }

            if ((sessionData(chatId).getAlarmId() == 7650) && (sessionData(chatId).getFlag() == 0)) {
                sessionData(chatId).setFlag(1);
                message = "alarm 7650 troubleshoot";
            }

            if (sessionData(chatId).getAlarmId() == 70359 && sessionData(chatId).getFlag() == 0) {
                sessionData(chatId).setHist("do you want to troubleshoot it or check the description?");
                sessionData(chatId).setFlag(1);
                if (sessionData(chatId).getActionId() == 1) {
                    message = "70359 troubleshoot";
                }
                else {
                    message = "70359 description";
                }
            }
            if (sessionData(chatId).getAlarmId() == 3199 && sessionData(chatId).getFlag() == 0) {
                sessionData(chatId).setHist("do you want to troubleshoot alarm 3199 it or to check the description?");
                sessionData(chatId).setFlag(1);
                if (sessionData(chatId).getActionId() == 1) {
                    message = "3199 troubleshoot";
                }
                else {
                    message = "3199 description";
                }
            }
        }

        if (sessionData(chatId).getHist().contains("is the second field of alarm 00 or 01") &&
                (message.toLowerCase().contains("cfpu0") ||
                message.toLowerCase().contains("cfpu-0") ||
                message.toLowerCase().contains("0") ||
                message.toLowerCase().contains("cfpu1") ||
                message.toLowerCase().contains("cfpu-1") ||
                message.toLowerCase().contains("1") ||
                message.toLowerCase().contains("first") ||
                message.toLowerCase().contains("last")) == false) {
            results.add("Sorry this is not a valid configuration. I will inform nokia. Your case number is NA081984827.");
            resetMemory();
            message = "";
        }

        if (sessionData(chatId).getHist().contains("Can you please tell me the alarm number if you are refering to? You can type 70359 or 3199.")
                && (message.toLowerCase().contains("70359")
                || message.toLowerCase().contains("3199")) == false) {
            if (sessionData(chatId).getNtries() < 1) {
                results.add("Sorry you need to tell me the alarm ID. You can type either 70359 or 3199.");
                sessionData(chatId).setNtries(sessionData(chatId).getNtries() + 1);
                message = "";
            }
            else {
                results.add("Ok. lets talk about something else.");
                resetMemory();
                sessionData(chatId).setCurrentAI(1);
                message = "";
            }
        }

        if (sessionData(chatId).getHist().contains("is it cfpu-0 or cfpu-1") &&
                (message.toLowerCase().contains("cfpu0") ||
                message.toLowerCase().contains("cfpu-0") ||
                message.toLowerCase().contains("0") ||
                message.toLowerCase().contains("cfpu1") ||
                message.toLowerCase().contains("cfpu-1") ||
                message.toLowerCase().contains("1") ||
                message.toLowerCase().contains("first") ||
                message.toLowerCase().contains("last")) == false) {
            results.add("is the second field of alarm 00 or 01?");
            sessionData(chatId).setHist("is the second field of alarm 00 or 01?");
            message = "";
        }

        if (sessionData(chatId).getCurrentAI() == 0 && sessionData(chatId).getActionId() > 0 && sessionData(chatId).getAlarmId() > 0 && message != "") {
            HttpURLConnection connection = null;
            Map output = null;

            try {
                String chatHistory = URLEncoder.encode(sessionData(chatId).getHist(), "UTF-8");
                String currentTopic = URLEncoder.encode(sessionData(chatId).getT(), "UTF-8");
                String q = URLEncoder.encode(message, "UTF-8");
                URL url = new URL("https://api.hutoma.com/nokia/ai/8fa2a7c0-b681-4d5a-9b60-b61babfaf9ce/chat?confidence_threshold=0.55&chat_history=" + chatHistory + "&current_topic=" + currentTopic + "&uid=87142473&q=" + q);

                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8QE-jn7xhko6SsWlSUAxF1dffyMTCzPLVANzXYMUgzRdkzRjc90ko7RE3WSLpDSjJHNDY4OUFKVaAAAAAP__.7dc5arNyLKOUk6Df-DPSuddb5HD3enC3OaQGVMYhhys");
                connection.setReadTimeout(30*1000);

                try (InputStream is = connection.getInputStream()) {

                    StringBuilder response = new StringBuilder();
                    String line;
                    try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {

                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                        }
                    }
                    output = (Map) JSON.parse(response.toString());
                }
            }
            catch (Exception e) {
                throw e;
            }

            Map result = (Map)output.get("result");
            String res = null;

            sessionData(chatId).setT("");
            if (result.containsKey("topic_out")) {
                sessionData(chatId).setT((String) result.get("topic_out"));
            }

            res = "Sorry i dont have an answer for you";
            res = (String)result.get("answer");

            if (res.contains("@reset")) {
                res = res.replace("@reset", "");
                resetMemory();
                results.add(res);
            }
            else {
                if (res.contains("@pause")) {
                    sessionData(chatId).setCurrentAI(1);
                    res = res.replace("@pause", "");
                    resetMemory();
                    results.add(res);
                    results.add("Nokia is informed. Your case number is NA081984827");
                }
                else {
                    double sc = (double)result.get("score");
                    if (sc > 0.6f) {
                        sessionData(chatId).setHist(((String) result.get("answer")).trim());
                    }


                    if (sc > 0.6f) {
                        sessionData(chatId).setCurrentAI(0);
                        sessionData(chatId).setNtries(0);
                        results.add(res);
                    }
                    else {
                        if (sessionData(chatId).getNtries() < 3) {
                            sessionData(chatId).setNtries(sessionData(chatId).getNtries() + 1);
                            if (sessionData(chatId).getNtries() == 1) {
                                results.add("Can you please rephrase your question?");
                            }
                            else {
                                resetMemory();
                                sessionData(chatId).setCurrentAI(1);
                                results.add("Sorry I dont think I have been trained to recognise this phrase. I will contact Nokia.");
                            }
                        } else {
                            sessionData(chatId).setNtries(0);
                            sessionData(chatId).setHist("");
                            sessionData(chatId).setCurrentAI(1);
                            results.add(AITalk(message, sessionData(chatId).getUserid()));
                        }
                    }
                }
            }
        }
        else {
            if (sessionData(chatId).getActionId() < 0 && sessionData(chatId).getAlarmId() < 0 && message != "") {
                results.add(AITalk(message, sessionData(chatId).getUserid()));
            }
        }

        StringBuilder result = new StringBuilder();
        for (String item : results) {
            result.append(item);
            result.append("\r\n");
        }
        return result.toString();
    }

    public String AITalk(String message, int userId) throws IOException {
        String answer = "";
        HttpURLConnection connection = null;
        Map output = null;

        try {
            String uid = URLEncoder.encode(Integer.toString(sessionData(chatId).getUserid()), "UTF-8");
            String aid = URLEncoder.encode("384", "UTF-8");
            String q = URLEncoder.encode(message, "UTF-8");
            URL url = new URL("https://www.hutoma.com:8443/api/hutoma/demochat?uid=" + uid + "&aid=" + aid + "&q=" + q);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30*1000);

            try (InputStream is = connection.getInputStream()) {
                StringBuilder response = new StringBuilder();
                String line;
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                }
                String jsonp = response.toString();
                String json = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
                output = (Map) JSON.parse(json);
            }
        }
        catch (Exception e) {
            throw e;
        }

        String response = (String)output.get("output");
        return response.substring(response.indexOf("]") + 1);
    }

    private AssistantState sessionData(String chatId) {
        return this.assistantSessions.sessions.get(chatId);
    }
    // @TODO /demo hack

    private ChatResult interpretSemanticResult() throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitWnet();

        if (chatResult.getAnswer() != null) {
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
        } else {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            this.telemetryMap.put("WNETResponseNULL", "true");
        }

        this.logger.logDebug(LOGFROM, String.format("WNET response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("WNETAnswer", chatResult.getAnswer());
        this.telemetryMap.put("WNETTopicOut", chatResult.getTopicOut());
        this.telemetryMap.put("WNETElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretAimlResult() throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitAiml();

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("AIML response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("AIMLAnswer", chatResult.getAnswer());
        this.telemetryMap.put("AIMLElapsedTime", Double.toString(chatResult.getElapsedTime()));
        return chatResult;
    }

    private ChatResult interpretRnnResult() throws ServerConnector.AiServicesException {

        // wait for result to complete
        ChatResult chatResult = this.chatServices.awaitRnn();
        if (chatResult.getAnswer() == null) {
            chatResult.setAnswer("");
            chatResult.setScore(0.0);
            this.telemetryMap.put("RNNResponseNULL", "true");
        }

        // always reset the conversation if we have gone with a non-wnet result
        chatResult.setResetConversation(true);

        // remove trailing newline
        chatResult.setAnswer(chatResult.getAnswer().trim());

        this.logger.logDebug(LOGFROM, String.format("RNN response in time %f with confidence %f",
                toOneDecimalPlace(chatResult.getElapsedTime()), toOneDecimalPlace(chatResult.getScore())));

        this.telemetryMap.put("RNNElapsedTime", Double.toString(chatResult.getElapsedTime()));
        this.telemetryMap.put("RNNAnswer", chatResult.getAnswer());
        this.telemetryMap.put("RNNTopicOut", chatResult.getTopicOut());

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
    private boolean handleIntents(final ChatResult chatResult, final String devId, final UUID aiid, final UUID chatUuid,
                                  final String question, final Map<String, String> telemetryMap) throws IntentException {

        // the reply that we are processing is the one to return to the user
        boolean replyConfidence = true;

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
                        // if we run out of n_prompts we just stop asking.
                        // the user can still answer the question ... or not
                        telemetryMap.put("IntentNotFulfilled", memoryIntent.getName());
                        replyConfidence = false;
                    }

                }
            }
            this.intentHandler.updateStatus(memoryIntent);
        }

        // Add the current intents state to the chat response
        chatResult.setIntents(this.intentHandler.getCurrentIntentsStateForChat(aiid, chatUuid));
        return replyConfidence;
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



