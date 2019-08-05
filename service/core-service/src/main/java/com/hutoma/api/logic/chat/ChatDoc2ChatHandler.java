package com.hutoma.api.logic.chat;

import com.google.common.annotations.VisibleForTesting;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.memory.IMemoryIntentHandler;

import javax.inject.Inject;

public class ChatDoc2ChatHandler extends ChatGenericBackend implements IChatHandler {

    private static final String LOGFROM = "chatdoc2chathandler";
    private final ILogger logger;
    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentLogic;
    private final ContextVariableExtractor contextVariableExtractor;

    @VisibleForTesting
    @Inject
    public ChatDoc2ChatHandler(final IMemoryIntentHandler intentHandler,
                               final IntentProcessor intentLogic,
                               final ContextVariableExtractor contextVariableExtractor,
                               final ILogger logger) {
        this.intentHandler = intentHandler;
        this.intentLogic = intentLogic;
        this.contextVariableExtractor = contextVariableExtractor;
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap) {
        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return false;
    }
}

