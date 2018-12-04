package com.hutoma.api.logic.chat;

import com.hutoma.api.common.ChatLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.FacebookChatHandler;
import com.hutoma.api.memory.ChatStateHandler;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WebhookHandler {

    private static final String LOGFROM = "webhookhandler";

    private final ChatStateHandler chatStateHandler;
    private final FacebookChatHandler facebookChatHandler;
    private final ChatLogger chatLogger;
    private final Config config;
    private final FeatureToggler featureToggler;

    @Inject
    WebhookHandler(final ChatStateHandler chatStateHandler,
                   final FacebookChatHandler facebookChatHandler,
                   final Config config,
                   final ChatLogger chatLogger,
                   final FeatureToggler featureToggler) {
        this.chatStateHandler = chatStateHandler;
        this.facebookChatHandler = facebookChatHandler;
        this.chatLogger = chatLogger;
        this.config = config;
        this.featureToggler = featureToggler;
    }

    void updateChatContext(final ChatContext sessionCtx, final ChatContext responseCtx) {
        // Now set the chat context variables based on the ones returned by the webhook

        if (responseCtx != null) {
            for (Map.Entry<String, String> entry : responseCtx.getVariablesAsStringMap().entrySet()) {
                String varName = entry.getKey();
                if (sessionCtx.isSet(varName)) {
                    // If we get a null variable, this then needs to be cleared
                    if (entry.getValue() == null) {
                        sessionCtx.clearVariable(varName);
                    } else {
                        // Update value
                        sessionCtx.setValue(varName, entry.getValue(),
                                sessionCtx.getVariable(varName).getLifespanTurns());
                    }
                } else if (entry.getValue() != null) {
                    // add a new value
                    sessionCtx.setValue(varName, entry.getValue(),
                            ChatContext.ChatVariableValue.DEFAULT_LIFESPAN_TURNS);
                }
            }
        }
    }

    public ApiResult runWebhookCallback(final String chatIdHash,
                                        final WebHookResponse webHookResponse) {
        LogMap logMap = LogMap.map("ChatIdHash", chatIdHash).put("ChatOrigin", "Webhook");
        ChatState state = null;
        UUID aiid = null;
        String devIdString = null;
        WebHookSession webHookSession = null;
        try {
            state = this.chatStateHandler.getState(chatIdHash);
            if (state == null) {
                this.chatLogger.logInfo(LOGFROM, "doCallback - invalid chat id hash", logMap);
                return ApiError.getBadRequest("Invalid chat session token");
            }

            devIdString = state.getDevId().toString();
            Optional<WebHookSession> optWebHookSession = state.getWebhookSessions().stream()
                    .filter(x -> x.getToken().equals(webHookResponse.getToken())).findFirst();
            if (!optWebHookSession.isPresent()) {
                this.chatLogger.logUserTraceEvent(LOGFROM, "doCallback - webhook token not valid", devIdString, logMap);
                return ApiError.getBadRequest("Invalid webhook session token");
            }
            aiid = UUID.fromString(state.getAi().getAiid());

            if (this.featureToggler.getStateForAiid(state.getDevId(), aiid, "webhook-callback")
                    != FeatureToggler.FeatureState.T1) {
                this.chatLogger.logUserTraceEvent(LOGFROM, "doCallback - webhook callback not allowed",
                        devIdString, logMap);
                return ApiError.getForbidden("Bot not authorized for callback");
            }

            webHookSession = optWebHookSession.get();

            if (state.getIntegrationData() == null) {
                this.chatLogger.logUserTraceEvent(LOGFROM, "doCallback - session without integration data",
                        devIdString, logMap);
                return ApiError.getBadRequest("Session not supported for callback");
            }
            if (state.getIntegrationData().getIntegrationType() != IntegrationType.FACEBOOK) {
                this.chatLogger.logUserTraceEvent(LOGFROM, "doCallback - session not integrated with Facebook",
                        devIdString,
                        logMap.put("IntegrationType", state.getIntegrationData().getIntegrationType().value()));
                return ApiError.getBadRequest("Session not supported for callback");
            }

            // Currently we only support Facebook integration
            IntegrationDataFacebook fbIntegrationData = new IntegrationDataFacebook(state.getIntegrationData());

            this.facebookChatHandler.processWebhookCallbackResponse(aiid, webHookResponse,
                    fbIntegrationData.getPageToken(),
                    fbIntegrationData.getMessageOriginatorId());

            updateChatContext(state.getChatContext(), webHookResponse.getChatContext());

            return new ApiResult().setSuccessStatus();
        } catch (Exception ex) {
            this.chatLogger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        } finally {
            try {

                if (state != null && aiid != null) {
                    if (webHookSession != null) {
                        // Decrement the uses
                        webHookSession.decrementUses();
                        state.getWebhookSessions().removeIf(x -> x.getMaxUses() <= 0);

                        // log interaction
                        this.chatLogger.logUserInfoEvent(LOGFROM, "ApiWebhook", devIdString,
                                logMap.put("ChatId", state.getChatId())
                                        .put("AIID", aiid)
                                        .put("ResponseSent", webHookResponse.getText() != null
                                                ? webHookResponse.getText() : "(none)")
                                        .put("Facebook_RichWebhook", webHookResponse.getFacebookNode() != null)
                                        .put("WebhookSession.ReminingUses", webHookSession.getMaxUses()));
                    }
                    this.chatStateHandler.saveState(state.getDevId(), aiid, state.getChatId(), state);
                }
            } catch (ChatStateHandler.ChatStateException ex) {
                this.chatLogger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devIdString, ex);
            }
        }
    }
}
