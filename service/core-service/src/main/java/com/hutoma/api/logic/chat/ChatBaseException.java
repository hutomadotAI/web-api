package com.hutoma.api.logic.chat;

import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.logging.CentralLogger;
import com.hutoma.api.logging.LogMap;

public abstract class ChatBaseException extends Exception {

    public ChatBaseException() {
    }

    public ChatBaseException(final String message) {
        super(message);
    }

    public ChatBaseException(final Throwable cause) {
        super(cause);
    }

    protected ChatBaseException(String message, Throwable e) {
        super(message, e);
    }

    static LogMap getNetExceptionLogMap(final ChatRequestInfo requestInfo,
                                                       final String url,
                                                       final WebHooks.WebHookExternalException ex) {
        String reason = "";
        if (ex.getCause() instanceof java.net.SocketTimeoutException) {
            reason = "timeout";
        } else if (ex.getCause() instanceof java.net.ConnectException) {
            reason = "could not connect";
        } else if (ex.getCause() instanceof java.net.NoRouteToHostException) {
            reason = "no route to host";
        } else if (ex.getCause() instanceof java.net.ProtocolException) {
            reason = "protocol exception";
        } else if (ex.getCause() instanceof java.net.UnknownHostException) {
            reason = "unknown host";
        } else if (ex.getCause() instanceof java.net.UnknownServiceException) {
            reason = "unknown service";
        } else {
            reason = "other";
        }

        return LogMap.map("AIID", requestInfo.getAiid())
                .put("ChatId", requestInfo.getChatId())
                .put("Question", requestInfo.getQuestion())
                .put("URL", url)
                .put("Reason", reason)
                .put("Message", ex.getMessage())
                .put("StackTrace", CentralLogger.getStackTraceAsString(ex.getStackTrace()));
    }
}
