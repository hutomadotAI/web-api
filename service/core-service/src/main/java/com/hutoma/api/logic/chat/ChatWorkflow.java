package com.hutoma.api.logic.chat;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

/**
 * This class defines the chat workflow.
 * The handlers list provides the sequence of handlers which the chat workflow
 * will go through. Each handler then has the option to terminate the flow (by returning TRUE
 * in chatCompleted(), otherwise the flow will proceed to the next handler.
 * There should always be a default handler that will terminate the flow in case no other handlers do.
 * <p>
 * To add new handlers (other backends, for instance), one just needs to inject the handlers class
 * to the ctor and add it to the handlers list in the respective position (priority)
 */
public class ChatWorkflow {

    private List<IChatHandler> handlers;

    @Inject
    public ChatWorkflow(final ChatEntityValueHandler entityHandler,
                        final ChatHandoverHandler handoverHandler,
                        final ChatPassthroughHandler passthroughHandler,
                        final ChatIntentHandler intentHandler,
                        final ChatRequestTrigger requestBETrigger,
                        final ChatEmbHandler embHandler,
                        final ChatAimlHandler aimlHandler,
                        final ChatDefaultHandler defaultHandler) {

        // Handlers are executed based on its position in the list
        this.handlers = Arrays.asList(
                entityHandler,
                handoverHandler,
                passthroughHandler,
                intentHandler,
                requestBETrigger,
                embHandler,
                aimlHandler,
                defaultHandler
        );
    }

    public List<IChatHandler> getHandlers() {
        return this.handlers;
    }
}
