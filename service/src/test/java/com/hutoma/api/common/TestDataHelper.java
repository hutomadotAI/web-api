package com.hutoma.api.common;

import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.IServerEndpoint;

import org.joda.time.DateTime;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by pedrotei on 09/01/17.
 */
public class TestDataHelper {
    public static final UUID DEVID_UUID = UUID.fromString("113d39cb-7f43-40d7-8dee-17b25b205581");
    public static final String DEVID = DEVID_UUID.toString();

    public static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    public static final UUID SESSIONID = UUID.fromString("e6a7d7b4-245a-44ad-8018-5c0516583713");
    public static final UUID ALT_SESSIONID = UUID.fromString("f29a1eed-6094-464a-b335-c0885a501750");

    public static ApiAi getSampleAI() {
        return new ApiAi(TestDataHelper.AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                new BackendStatus(), true,
                0, 0.0, 1, Locale.getDefault(), "UTC", null);
    }

    public static ApiAi getAi(TrainingStatus status, boolean isPrivate) {
        BackendStatus result = new BackendStatus();
        result.setEngineStatus(BackendServerType.WNET, new BackendEngineStatus(status, 0.0, 0.0));
        result.setEngineStatus(BackendServerType.RNN, new BackendEngineStatus(status, 0.0, 0.0));
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), isPrivate, result, true,
                0, 0.0, 1, Locale.getDefault(), "UTC", null);
    }

    public static ApiAi getAI() {
        return getAi(null);
    }

    public static ApiAi getAi(final BackendStatus backendStatus) {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                backendStatus, true, 1, 0.1, 1, Locale.UK, "Europe/London", null);
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


}
