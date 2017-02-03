package com.hutoma.api.common;

import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by pedrotei on 09/01/17.
 */
public class TestDataHelper {
    public static final String DEVID = "devid";
    public static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");

    public static ApiAi getSampleAI() {
        return new ApiAi(TestDataHelper.AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                new BackendStatus(), true,
                0, 0.0, 1, Locale.getDefault(), "UTC");
    }

    public static ApiAi getAi(TrainingStatus status, boolean isPrivate) {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), isPrivate, new BackendStatus(), true,
                0, 0.0, 1, Locale.getDefault(), "UTC") {
            @Override
            public TrainingStatus getSummaryAiStatus() {
                return status;
            }
        };
    }

    public static ApiAi getAI() {
        return getAi(null);
    }

    public static ApiAi getAi(final BackendStatus backendStatus) {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false,
                backendStatus, true, 1, 0.1, 1, Locale.UK, "Europe/London");
    }

    private static BackendStatus setBackendEngineStatus(final TrainingStatus status) {
        BackendStatus bs = new BackendStatus();
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendStatus.ENGINE_AIML, 0.0, 1.0));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendStatus.ENGINE_WNET, 0.0, 1.0));
        bs.setEngineStatus(new AiStatus(DEVID, AIID, status, BackendStatus.ENGINE_RNN, 0.0, 1.0));
        return bs;
    }

    public static BackendStatus getTrainingCompleted() {
        return setBackendEngineStatus(TrainingStatus.AI_TRAINING_COMPLETE);
    }

    public static BackendStatus getTrainingInProgress() {
        return setBackendEngineStatus(TrainingStatus.AI_TRAINING);
    }

    public List<UUID> getUUIDList() {
        return Arrays.asList(
                UUID.fromString("e02324ac-aea3-4503-9e3b-73002d3a9380"),
                UUID.fromString("97e8e03c-7c7f-4047-b6d7-97a529314c51"),
                UUID.fromString("eda34590-751c-48a2-ae93-a19d42d5bd6f"),
                UUID.fromString("65af68fc-ccd5-4d86-9be3-e270cc8becb7"),
                UUID.fromString("3a42374b-fcd1-4c7e-82b0-2cbe2e2a1e40"),
                UUID.fromString("a3ce7dad-e0bb-4a2b-8a23-ad2e8605ab56"),
                UUID.fromString("baa45957-5def-46aa-8e48-001d1ad9c1ae"),
                UUID.fromString("7517e2c5-b0bd-49bc-b6c2-0c0546e35a0f"),
                UUID.fromString("56649dc5-5ea5-48c6-bf01-d4ceb5cb3e26"),
                UUID.fromString("7acf5776-00d9-4d49-9af6-b72f6cbc0d41"));
    }
}
