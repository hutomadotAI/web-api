package com.hutoma.api.common;

import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.logic.AIBotStoreLogic;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID;

/**
 * Created by pedrotei on 08/01/17.
 */
public final class BotHelper {

    public static final int BOTID = 1234;

    public static final AiBot SAMPLEBOT =
            new AiBot(DEVID, AIID, BOTID, "name", "description", "long description", "alert message", "badge",
                    BigDecimal.valueOf(1.123), "sample", "category", "licType", DateTime.now(), "privacy policy",
                    "classification", "version", "http://video", true, null);
    private static final byte[] BOTICON_CONTENT = "this is an image!".getBytes(Charset.defaultCharset());

    public static ApiResult publishSampleBot(final AIBotStoreLogic aiBotStoreLogic) {
        return aiBotStoreLogic.publishBot(SAMPLEBOT.getDevId(), SAMPLEBOT.getAiid(), SAMPLEBOT.getName(),
                SAMPLEBOT.getDescription(), SAMPLEBOT.getLongDescription(), SAMPLEBOT.getAlertMessage(), SAMPLEBOT.getBadge(),
                SAMPLEBOT.getPrice(), SAMPLEBOT.getSample(), SAMPLEBOT.getCategory(), SAMPLEBOT.getLicenseType(),
                SAMPLEBOT.getPrivacyPolicy(), SAMPLEBOT.getClassification(), SAMPLEBOT.getVersion(), SAMPLEBOT.getVideoLink());
    }

    public static byte[] getBotIconContent() {
        return BOTICON_CONTENT.clone();
    }
}
