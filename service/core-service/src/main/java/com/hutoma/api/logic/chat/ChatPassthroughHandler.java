package com.hutoma.api.logic.chat;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.chat.AIChatServices;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.containers.sub.WebHookResponse;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;

import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;

public class ChatPassthroughHandler implements IChatHandler {

    private static final String LOGFROM = "chatpassthrough";

    private final AIChatServices chatServices;
    private final WebHooks webHooks;
    private final ChatLogger chatLogger;
    private final ILogger logger;
    private final Tools tools;
    private boolean hasPassthrough;

    @Inject
    public ChatPassthroughHandler(final AIChatServices chatServices, final WebHooks webhooks, final Tools tools,
                                  final ChatLogger chatLogger, final ILogger logger) {
        this.chatServices = chatServices;
        this.webHooks = webhooks;
        this.tools = tools;
        this.chatLogger = chatLogger;
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ChatLogic.ChatFailedException, WebHooks.WebHookException {

        String passthrough = this.chatServices.getAIPassthroughUrl(requestInfo.getDevId(), requestInfo.getAiid());

        if (!StringUtils.isEmpty(passthrough)) {
            this.hasPassthrough = true;

            final String devIdString = requestInfo.getDevId().toString();

            ChatResult chatResult = new ChatResult(requestInfo.getQuestion());
            final ChatRequestInfo chatInfo = new ChatRequestInfo(
                    new AiIdentity(requestInfo.getDevId(), requestInfo.getAiid()),
                    requestInfo.getChatId(), requestInfo.getQuestion(),
                    requestInfo.getClientVariables());
            final long startTime = this.tools.getTimestamp();

            // Add telemetry for the request
            telemetryMap.add("DevId", devIdString);
            telemetryMap.add("AIID", requestInfo.getAiid());
            telemetryMap.add("ChatId", requestInfo.getChatId());
            telemetryMap.add("Q", requestInfo.getQuestion());
            telemetryMap.add("PassthroughUrl", passthrough);
            telemetryMap.add("ChatType", "Passthrough");

            try {
                WebHookResponse response = this.webHooks.executePassthroughWebhook(passthrough, chatResult, chatInfo);

                if (response != null) {
                    // copy the text reply
                    chatResult.setAnswer(response.getText());
                    // and copy the whole response to include any rich content
                    chatResult.setWebHookResponse(response);
                }
            } catch (WebHooks.WebHookExternalException callException) {
                // Log net exception details
                LogMap logMap = ChatBaseException.getNetExceptionLogMap(requestInfo, passthrough, callException);
                this.logger.logUserTraceEvent(LOGFROM, "External exception in passthrough",
                        devIdString, logMap);
                // Log in the chatlogger
                this.chatLogger.logUserWarnEvent(LOGFROM, String.format("External error in passthrough - %s",
                        callException.getMessage()), requestInfo.getDevId().toString(), telemetryMap);
                throw callException;
            } catch (WebHooks.WebHookException webhookException) {
                this.logger.logUserErrorEvent(LOGFROM,
                        "Error occurred executing WebHook for passthrough",
                        chatInfo.getDevId().toString(),
                        LogMap.map("AIID", requestInfo.getAiid())
                                .put("PassthroughUrl", passthrough)
                                .put("Error", webhookException.getMessage()));
                this.chatLogger.logChatError(LOGFROM, requestInfo.getDevId().toString(),
                        webhookException, telemetryMap);
                throw new ChatLogic.ChatFailedException(ApiError.getInternalServerError());
            }

            // set the chat response time to the whole duration since the start of the request until now
            chatResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);

            telemetryMap.add("RequestDuration", chatResult.getElapsedTime());
            telemetryMap.add("ResponseSent", chatResult.getAnswer());
            telemetryMap.add("Score", chatResult.getScore());

            // log the results
            this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, telemetryMap);
            this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString,
                    LogMap.map("AIID", requestInfo.getAiid())
                            .put("SessionId", requestInfo.getChatId()));

            return chatResult;
        }

        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return hasPassthrough;
    }
}
