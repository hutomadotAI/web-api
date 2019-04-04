package com.hutoma.api.logic.chat;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.memory.IMemoryIntentHandler;

import javax.inject.Inject;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * SysAnyCommands allows to intercept the conversation flow in search for an
 * intent pattern like
 * trigger_command @{sys.any}@
 * These are typically sent by automated processes (typically webhooks) to trigger
 * an action in the conversation, and require it to be precise instead of going through
 * the NLP processing. For instance situations where the input flow is constructed artificially like:
 * ADD_PRODUCT_TO_CART PROD_ID_1234
 * we cannot rely on the NLP to try to extract a conversational intent from it, but we need to
 * trigger the appropriate intent to allow the external process to move forward.
 * Note that we require explicitly that the intent trigger follows a very rigid format, otherwise we
 * will fallback to the default chat behaviour
 */
public class SysAnyCommandsHandler implements IChatHandler {

    private final IMemoryIntentHandler intentHandler;
    private final IntentProcessor intentProcessor;
    private final IntentLogic intentLogic;
    private boolean answered;
    private final FeatureToggler featureToggler;

    @Inject
    public SysAnyCommandsHandler(final IMemoryIntentHandler intentHandler,
                                 final IntentProcessor intentProcessor,
                                 final IntentLogic intentLogic,
                                 final FeatureToggler featureToggler) {
        this.intentHandler = intentHandler;
        this.intentProcessor = intentProcessor;
        this.featureToggler = featureToggler;
        this.intentLogic = intentLogic;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap)
            throws WebHooks.WebHookException, ChatLogic.IntentException {

        // Tie it to a feature toggle
        if (featureToggler.getStateForAiid(
                requestInfo.getDevId(),
                requestInfo.getAiid(),
                "SYSANY_COMMANDS") == FeatureToggler.FeatureState.T1) {


            ApiResult apiResult = this.intentLogic.getIntents(
                    requestInfo.getDevId(), requestInfo.getAiid()
            );
            if (apiResult.getStatus().getCode() == HttpURLConnection.HTTP_OK) {
                String questionLowercase = requestInfo.getQuestion().toLowerCase();
                ApiIntentList intentList = (ApiIntentList)apiResult;
                for (ApiIntent intent: intentList.getIntents()) {
                    // construct a lowercase userSays list to avoid having to do this every iteration
                    List<String> userSays = new ArrayList<>(intent.getUserSays());
                    userSays.replaceAll(String::toLowerCase);
                    // Check only the ones who have a sys.any variable
                    for (IntentVariable intentVariable: intent.getVariables()) {
                        if (intentVariable.getEntityName().equalsIgnoreCase(IntentProcessor.SYSANY)) {
                            // We now have a SYS.ANY variable, check if the question matches the intent
                            for (String us: userSays) {
                                String triggerString = us + ' '; //
                                if (questionLowercase.startsWith(us)) {
                                    // Found it! - construct an intent and fire it away
                                    MemoryVariable memoryVariable = new MemoryVariable(
                                            intentVariable.getEntityName(),
                                            null
                                    );
                                    memoryVariable.setSystem(true);
                                    memoryVariable.setLabel(intentVariable.getLabel());
                                    String value = requestInfo.getQuestion().substring(triggerString.length());
                                    memoryVariable.setCurrentValue(value.trim());
                                    MemoryIntent memoryIntent = new MemoryIntent(
                                            intent.getIntentName(),
                                            requestInfo.getAiid(),
                                            requestInfo.getChatId(),
                                            Collections.singletonList(memoryVariable),
                                            true);

                                    if (intentProcessor.processIntent(
                                            requestInfo,
                                            requestInfo.getAiid(),
                                            memoryIntent,
                                            currentResult,
                                            telemetryMap)) {


                                        this.answered = true;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        return this.answered;
    }
}
