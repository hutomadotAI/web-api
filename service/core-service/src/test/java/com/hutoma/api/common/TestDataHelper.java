package com.hutoma.api.common;

import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.AiBotConfigDefinition;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.joda.time.DateTime;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

;

/**
 * Created by pedrotei on 09/01/17.
 */
public class TestDataHelper {
    public static final UUID DEVID_UUID = UUID.fromString("113d39cb-7f43-40d7-8dee-17b25b205581");
    public static final String DEVID = DEVID_UUID.toString();
    public static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    public static final AiIdentity AI_IDENTITY = new AiIdentity(DEVID_UUID, AIID);
    public static final UUID SESSIONID = UUID.fromString("e6a7d7b4-245a-44ad-8018-5c0516583713");
    public static final UUID ALT_SESSIONID = UUID.fromString("f29a1eed-6094-464a-b335-c0885a501750");
    public static final String DEFAULT_CHAT_RESPONSE = ChatDefaultHandler.COMPLETELY_LOST_RESULT;
    public static final List<String> DEFAULT_CHAT_RESPONSES = Collections.singletonList(DEFAULT_CHAT_RESPONSE);
    public static final AiBotConfigDefinition DEFAULT_API_KEY_DESC = new AiBotConfigDefinition(null);
    public static final String VALID_ENCODING_KEY = "RW1wdHlUZXN0S2V5";

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
        when(fakeDatabase.getAI(any(UUID.class), any(UUID.class), any(JsonSerializer.class))).thenReturn(ai);
        when(fakeDatabase.getAIWithStatus(any(UUID.class), any(UUID.class), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenReturn(ai);
        when(fakeDatabase.getAIWithStatus(any(UUID.class), any(UUID.class), any(JsonSerializer.class))).thenReturn(ai);
    }

    public static ApiAi getAi(TrainingStatus status, boolean isPrivate) {
        BackendStatus result = new BackendStatus();
        result.setEngineStatus(BackendServerType.EMB, new BackendEngineStatus(status, 0.0, 0.0));
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
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendServerType.EMB, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 1.0, "hash", SESSIONID));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendServerType.AIML, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 1.0, "hash", SESSIONID));
        return bs;
    }

    public static BackendStatus getBackendStatus(final TrainingStatus embStatus) {
        BackendStatus bs = new BackendStatus();
        bs.setEngineStatus(new AiStatus(DEVID, AIID, embStatus, BackendServerType.EMB, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 1.0, "hash", SESSIONID));
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

    public static void mockDatabaseCreateAIInTrans(final DatabaseAI fakeDatabase, final UUID createdAiid) throws DatabaseException {
        when(fakeDatabase.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), any(), any(), anyDouble(), anyInt(),
                anyInt(), any(), anyInt(), anyInt(), any(), any(), any())).thenReturn(createdAiid);
    }

    public static AiBot getAiBot(int id, String name) {
        return new AiBot(DEVID_UUID, AIID, id, name, "desc", "longdesc", "alert",
                "badge", new BigDecimal(0), "sample", "category", "licenseType",
                DateTime.now(), "privacyPolicy", "classification", "version",
                "videoLink", AiBot.PublishingState.NOT_PUBLISHED, AiBot.PublishingType.SKILL, "botIcon");
    }

    public static JerseyInvocation.Builder mockJerseyClient(JerseyClient fakeJerseyClient) {
        JerseyWebTarget jerseyWebTarget = Mockito.mock(JerseyWebTarget.class);
        JerseyInvocation.Builder builder = Mockito.mock(JerseyInvocation.Builder.class);
        when(fakeJerseyClient.target(any(String.class))).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.path(anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.queryParam(anyString(), anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.request()).thenReturn(builder);
        when(jerseyWebTarget.resolveTemplates(any())).thenReturn(jerseyWebTarget);
        return builder;
    }

    public static void setFeatureToggleToControl(final FeatureToggler featureToggler) {
        when(featureToggler.getStateForAiid(any(), any(), any())).thenReturn(FeatureToggler.FeatureState.C);
        when(featureToggler.getStateforDev(any(), any())).thenReturn(FeatureToggler.FeatureState.C);
        when(featureToggler.getState(any())).thenReturn(FeatureToggler.FeatureState.C);
    }
}
