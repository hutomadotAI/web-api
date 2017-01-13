package com.hutoma.api.common;

import com.hutoma.api.containers.ApiAi;
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
                new BackendStatus(), TrainingStatus.AI_UNDEFINED,
                0, 0.0, 1, Locale.getDefault(), "UTC");
    }

    public static ApiAi getAi(TrainingStatus status, boolean isPrivate) {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), isPrivate, new BackendStatus(), status,
                0, 0.0, 1, Locale.getDefault(), "UTC");
    }
}
