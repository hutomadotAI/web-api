package com.hutoma.api.logic.chat;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.FeatureToggler;
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
import javax.ws.rs.core.Feature;
import java.util.UUID;

public class ChatPassthroughHandler implements IChatHandler {

    private static final String LOGFROM = "chatpassthrough";

    private final AIChatServices chatServices;
    private final WebHooks webHooks;
    private final ChatLogger chatLogger;
    private final ILogger logger;
    private final Tools tools;
    private final FeatureToggler featureToggler;
    private boolean hasPassthrough;

    @Inject
    public ChatPassthroughHandler(final AIChatServices chatServices,
                                  final WebHooks webhooks,
                                  final Tools tools,
                                  final ChatLogger chatLogger,
                                  final ILogger logger,
                                  final FeatureToggler featureToggler) {
        this.chatServices = chatServices;
        this.webHooks = webhooks;
        this.tools = tools;
        this.chatLogger = chatLogger;
        this.logger = logger;
        this.featureToggler = featureToggler;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws ChatLogic.ChatFailedException, WebHooks.WebHookException {

        String passthrough = this.chatServices.getAIPassthroughUrl(requestInfo.getDevId(), requestInfo.getAiid());

        if (!Tools.isEmpty(passthrough)) {
            // Add telemetry for the request
            final UUID devId = requestInfo.getDevId();
            final String devIdString = devId.toString();
            telemetryMap.add("DevId", devIdString);
            telemetryMap.add("AIID", requestInfo.getAiid());
            telemetryMap.add("ChatId", requestInfo.getChatId());
            telemetryMap.add("Q", requestInfo.getQuestion());
            telemetryMap.add("PassthroughUrl", passthrough);
            telemetryMap.add("ChatType", "Passthrough");

            if (this.featureToggler.getStateforDev(devId, "enable-passthrough-url") != FeatureToggler.FeatureState.T1) {
                this.logger.logUserErrorEvent(LOGFROM,
                        "Passthrough call not allowed for this user",
                        devIdString,
                        LogMap.map("AIID", requestInfo.getAiid())
                                .put("PassthroughUrl", passthrough));
                this.chatLogger.logUserErrorEvent(LOGFROM, "Passthrough call not allowed for this user", devIdString,
                        telemetryMap);
                throw new ChatLogic.ChatFailedException(ApiError.getBadRequest(
                        "Passthrough call disallowed for this developer account"));
            }
            this.hasPassthrough = true;


            final ChatRequestInfo chatInfo = new ChatRequestInfo(
                    new AiIdentity(requestInfo.getDevId(), requestInfo.getAiid()),
                    requestInfo.getChatId(), requestInfo.getQuestion(),
                    requestInfo.getClientVariables());
            final long startTime = this.tools.getTimestamp();


            try {
                WebHookResponse response = this.webHooks.executePassthroughWebhook(
                        passthrough, currentResult, chatInfo);

                if (response != null) {
                    // copy the text reply
                    currentResult.setAnswer(response.getText());
                    // and copy the whole response to include any rich content
                    currentResult.setWebHookResponse(response);
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
                this.chatLogger.logChatError(LOGFROM, devIdString,
                        webhookException, telemetryMap);
                throw new ChatLogic.ChatFailedException(ApiError.getInternalServerError());
            }

            // set the chat response time to the whole duration since the start of the request until now
            currentResult.setElapsedTime((this.tools.getTimestamp() - startTime) / 1000.d);

            telemetryMap.add("RequestDuration", currentResult.getElapsedTime());
            telemetryMap.add("ResponseSent", currentResult.getAnswer());
            telemetryMap.add("Score", currentResult.getScore());

            // log the results
            this.chatLogger.logUserTraceEvent(LOGFROM, "ApiChat", devIdString, telemetryMap);
            this.logger.logUserTraceEvent(LOGFROM, "Chat", devIdString,
                    LogMap.map("AIID", requestInfo.getAiid())
                            .put("SessionId", requestInfo.getChatId()));
        }

        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return hasPassthrough;
    }
}
