package com.hutoma.api.common;

import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.AiBotConfigDefinition;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logic.ChatLogic;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

;

/**
 * Created by pedrotei on 09/01/17.
 */
public class TestDataHelper {
    public static final UUID DEVID_UUID = UUID.fromString("113d39cb-7f43-40d7-8dee-17b25b205581");
    public static final String DEVID = DEVID_UUID.toString();

    public static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    public static final UUID SESSIONID = UUID.fromString("e6a7d7b4-245a-44ad-8018-5c0516583713");
    public static final UUID ALT_SESSIONID = UUID.fromString("f29a1eed-6094-464a-b335-c0885a501750");
    public static final List<String> DEFAULT_CHAT_RESPONSES = Collections.singletonList(ChatLogic.COMPLETELY_LOST_RESULT);
    public static final AiBotConfigDefinition DEFAULT_API_KEY_DESC = new AiBotConfigDefinition(null);

    public static String stringOfLength(int length) {
        return String.join("", Collections.nCopies(length, "a"));
    }

    public static ApiAi getSampleAI() {
        return new ApiAi(TestDataHelper.AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                new BackendStatus(), true,
                0, 0.0, 1, Locale.getDefault(), "UTC", null, "", DEFAULT_CHAT_RESPONSES, DEFAULT_API_KEY_DESC);
    }

    public static void setupAiReadonlyMode(final DatabaseAI fakeDatabase) throws DatabaseException {
        ApiAi ai = new ApiAi(TestDataHelper.getSampleAI());
        ai.setReadOnly(true);
        when(fakeDatabase.getAI(any(), any(), any())).thenReturn(ai);
    }

    public static ApiAi getAi(TrainingStatus status, boolean isPrivate) {
        BackendStatus result = new BackendStatus();
        result.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(status, 0.0, 0.0));
        result.setEngineStatus(BackendServerType.RNN, new BackendEngineStatus(status, 0.0, 0.0));
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), isPrivate, result, true,
                0, 0.0, 1, Locale.getDefault(), "UTC", null, "", DEFAULT_CHAT_RESPONSES, DEFAULT_API_KEY_DESC);
    }

    public static ApiAi getAI() {
        return getAi(null);
    }

    public static ApiAi getAi(final BackendStatus backendStatus) {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                backendStatus, true, 1, 0.1, 1, Locale.UK, "Europe/London", null, "", DEFAULT_CHAT_RESPONSES, DEFAULT_API_KEY_DESC);
    }

    private static BackendStatus setBackendEngineStatus(final TrainingStatus status) {
        BackendStatus bs = new BackendStatus();
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendServerType.RNN, 0.0, 1.0, "hash", SESSIONID));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendServerType.WNET, 0.0, 1.0, "hash", SESSIONID));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendServerType.AIML, 0.0, 1.0, "hash", SESSIONID));
        return bs;
    }

    public static BackendStatus getBackendStatus(final TrainingStatus wnetStatus, final TrainingStatus rnnStatus) {
        BackendStatus bs = new BackendStatus();
        bs.setEngineStatus(new AiStatus(DEVID, AIID, rnnStatus, BackendServerType.RNN, 0.0, 1.0, "hash", SESSIONID));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, wnetStatus, BackendServerType.WNET, 0.0, 1.0, "hash", SESSIONID));
        return bs;
    }

    public static BackendStatus getTrainingCompleted() {
        return setBackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE);
    }

    public static BackendStatus getTrainingInProgress() {
        return setBackendEngineStatus(TrainingStatus.AI_TRAINING);
    }

    public static IServerEndpoint getEndpointFor(String url) {
        return new IServerEndpoint() {

            @Override
            public String getServerUrl() {
                return url;
            }

            @Override
            public String getServerIdentifier() {
                return "mock";
            }
        };
    }

    public static void mockDatabaseCreateAI(final DatabaseAI fakeDatabase, final UUID createdAiid) throws DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject(), any())).thenReturn(createdAiid);
    }

    public static AiBot getAiBot(int id, String name) {
        return new AiBot(DEVID_UUID, AIID, id, name, "desc", "longdesc", "alert",
                "badge", new BigDecimal(0), "sample", "category", "licenseType",
                DateTime.now(), "privacyPolicy", "classification", "version",
                "videoLink", AiBot.PublishingState.NOT_PUBLISHED, AiBot.PublishingType.SKILL, "botIcon");
    }
}
