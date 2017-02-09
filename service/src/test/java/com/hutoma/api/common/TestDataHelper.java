package com.hutoma.api.common;

import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

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

}
