package com.hutoma.api.containers.sub;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * AI Bot structure.
 */
public class AiBot {

    private final String name;
    private final String description;
    private final String longDescription;
    private final String alertMessage;
    private final String badge;
    private final BigDecimal price;
    private final String sample;
    private final String category;
    private final String licenseType;
    private final DateTime lastUpdate;
    private final String privacyPolicy;
    private final String classification;
    private final String version;
    private final String videoLink;
    private final String botIcon;
    @SerializedName("dev_id")
    private String devId;
    private PublishingState publishingState;
    private UUID aiid;
    private int botId;

    public AiBot(final String devId, final UUID aiid, final int botId, final String name, final String description,
                 final String longDescription, final String alertMessage, final String badge, final BigDecimal price,
                 final String sample, final String category, final String licenseType, final DateTime lastUpdate,
                 final String privacyPolicy, final String classification, final String version, final String videoLink,
                 final PublishingState publishingState, final String botIcon) {
        this.botId = botId;
        this.devId = devId;
        this.aiid = aiid;
        this.name = name;
        this.description = description;
        this.longDescription = longDescription;
        this.alertMessage = alertMessage;
        this.badge = badge;
        this.price = price;
        this.sample = sample;
        this.category = category;
        this.licenseType = licenseType;
        this.lastUpdate = lastUpdate;
        this.privacyPolicy = privacyPolicy;
        this.classification = classification;
        this.version = version;
        this.videoLink = videoLink;
        this.publishingState = publishingState;
        this.botIcon = botIcon;
    }

    public AiBot(final AiBot other) {
        this.botId = other.botId;
        this.devId = other.devId;
        this.aiid = other.aiid;
        this.name = other.name;
        this.description = other.description;
        this.longDescription = other.longDescription;
        this.alertMessage = other.alertMessage;
        this.badge = other.badge;
        this.price = other.price;
        this.sample = other.sample;
        this.category = other.category;
        this.licenseType = other.licenseType;
        this.lastUpdate = other.lastUpdate;
        this.privacyPolicy = other.privacyPolicy;
        this.classification = other.classification;
        this.version = other.version;
        this.videoLink = other.videoLink;
        this.publishingState = other.publishingState;
        this.botIcon = other.botIcon;
    }

    public int getBotId() {
        return this.botId;
    }

    public void setBotId(final int botId) {
        this.botId = botId;
    }

    public String getDevId() {
        return this.devId;
    }

    public void setDevId(final String devId) {
        this.devId = devId;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public void setAiid(final UUID aiid) {
        this.aiid = aiid;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLongDescription() {
        return this.longDescription;
    }

    public String getAlertMessage() {
        return this.alertMessage;
    }

    public String getBadge() {
        return this.badge;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public String getSample() {
        return this.sample;
    }

    public String getCategory() {
        return this.category;
    }

    public DateTime getLastUpdate() {
        return this.lastUpdate;
    }

    public String getPrivacyPolicy() {
        return this.privacyPolicy;
    }

    public String getClassification() {
        return this.classification;
    }

    public String getVersion() {
        return this.version;
    }

    public String getVideoLink() {
        return this.videoLink;
    }

    public PublishingState getPublishingState() {
        return this.publishingState;
    }

    public void setPublished(final PublishingState publishingState) {
        this.publishingState = publishingState;
    }

    public String getLicenseType() {
        return this.licenseType;
    }

    public String getBotIcon() {
        return this.botIcon;
    }

    public enum PublishingState {
        NOT_PUBLISHED(0),
        SUBMITTED(1),
        PUBLISHED(2),
        REMOVED(3);
        private final int value;

        PublishingState(final int value) {
            this.value = value;
        }

        public static PublishingState from(final int value) {
            Optional<PublishingState> state = Arrays.stream(PublishingState.values())
                    .filter(x -> x.value == value)
                    .findFirst();
            if (!state.isPresent()) {
                throw new IllegalArgumentException("Unknown publishing value");
            }
            return state.get();
        }

        public int value() {
            return this.value;
        }
    }
}
