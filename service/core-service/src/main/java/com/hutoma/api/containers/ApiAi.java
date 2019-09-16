package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.containers.sub.UITrainingState;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class ApiAi extends ApiResult {

    private String aiid;
    @SerializedName("client_token")
    private final String clientToken;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("passthroughUrl")
    private String passthroughUrl;
    @SerializedName("created_on")
    private DateTime createdOn;
    @SerializedName("is_private")
    private boolean isPrivate;
    @SerializedName("personality")
    private int personality; // aka. Learn from users
    @SerializedName("confidence")
    private double confidence; // aka Create new answers
    @SerializedName("voice")
    private int voice;
    @SerializedName("language")
    private Locale language;
    @SerializedName("timezone")
    private String timezone;
    @SerializedName("hmac_secret")
    private final String hmacSecret;
    // value from 0.0 to 1.0 representing training progress on the EMB server
    @SerializedName("phase_1_progress")
    private double phase1Progress;
    // true if the user has already successfully uploaded a training file for this ai
    @SerializedName("training_file_uploaded")
    private boolean trainingFileUploaded;
    @SerializedName("publishing_state")
    private AiBot.PublishingState publishingState;
    @SerializedName("default_chat_responses")
    private List<String> defaultChatResponses;
    @SerializedName("bot_config_definition")
    private AiBotConfigDefinition botConfigDefinition;
    @SerializedName("linked_bots")
    private List<Integer> linkedBots = new ArrayList<>();
    @SerializedName("readonly")
    private boolean readonly;
    @SerializedName("training")
    private UITrainingState uiTrainingState;
    @SerializedName("can_chat")
    private boolean canChat;
    // a public version of summaryStatus, with requeueing phases masked as "training"
    @SerializedName("ai_status")
    private TrainingStatus summaryStatusPublic;
    // how many seconds does a handover to non-AI operator gets reset after
    @SerializedName("handover_reset_timeout_seconds")
    private int handoverResetTimeoutSeconds;
    // how many consecutive times responses with low scores are allowed to be sent to the user
    // before automatically handing over to a non-AI operator (<0 never hand over)
    @SerializedName("error_threshold_handover")
    private int errorThresholdHandover = -1;
    @SerializedName("handover_message")
    private String handoverMessage;
    @SerializedName("engine_version")
    private String engineVersion;


    // transient because this should never be serialized along with the ApiAi object
    private transient BackendStatus backendStatus;
    // a single real status that represents the ai state on multiple backend servers
    private transient TrainingStatus summaryStatusReal;


    public ApiAi(final String aiid, final String clientToken) {
        this.aiid = aiid;
        this.clientToken = clientToken;
        this.hmacSecret = null;
    }

    public ApiAi(final String aiid, final String clientToken, final String name, final String description,
                 final DateTime createdOn, final boolean isPrivate, final BackendStatus backendStatus,
                 final boolean hasTrainingFile, final int personality, final double confidence, final int voice,
                 final Locale language, final String timezone, final String hmacSecret,
                 final String passthroughUrl, final List<String> defaultChatResponses,
                 final AiBotConfigDefinition botConfigDefinition) {
        this.aiid = aiid;
        this.clientToken = clientToken;
        this.name = name;
        this.description = description;
        this.createdOn = createdOn;
        this.isPrivate = isPrivate;
        this.personality = personality;
        this.confidence = confidence;
        this.voice = voice;
        this.language = language;
        this.timezone = timezone;
        this.trainingFileUploaded = hasTrainingFile;
        this.hmacSecret = hmacSecret;
        this.defaultChatResponses = defaultChatResponses;
        this.passthroughUrl = passthroughUrl;
        this.botConfigDefinition = botConfigDefinition;

        this.setBackendStatus(backendStatus);
        this.setEngineVersion(ServiceIdentity.DEFAULT_VERSION);
    }

    public ApiAi(final ApiAi other) {
        this.aiid = other.aiid;
        this.clientToken = other.clientToken;
        this.name = other.name;
        this.description = other.description;
        this.createdOn = other.createdOn;
        this.isPrivate = other.isPrivate;
        this.personality = other.personality;
        this.confidence = other.confidence;
        this.voice = other.voice;
        this.language = other.language;
        this.timezone = other.timezone;
        this.trainingFileUploaded = other.trainingFileUploaded;
        this.hmacSecret = other.hmacSecret;
        this.defaultChatResponses = other.defaultChatResponses;
        this.passthroughUrl = other.passthroughUrl;
        this.botConfigDefinition = other.botConfigDefinition;
        this.engineVersion = other.engineVersion;

        this.setBackendStatus(other.backendStatus);
    }

    /***
     * Reports "summary status" for both back-end servers by taking the one that is furthest behind.
     * @param embStatus
     * @return
     */
    private static TrainingStatus getSummaryTrainingStatus(final TrainingStatus embStatus) {
        return embStatus;
    }

    public UITrainingState getUiTrainingState() {
        return this.uiTrainingState;
    }

    public boolean isCanChat() {
        return this.canChat;
    }

    public List<Integer> getLinkedBots() {
        return this.linkedBots;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public void setLinkedBots(final List<Integer> linkedBots) {
        this.linkedBots = linkedBots == null ? new ArrayList<>() : linkedBots;

        // if we have any linked bots then set can_chat to true
        // otherwise leave it as it was set in the constructor
        if (!this.linkedBots.isEmpty()) {
            this.canChat = true;
        }
    }

    /***
     * Get the version of training status that will be reported to the caller
     * Note that this masks some of the internal workings (requeuing of bot training)
     * @return the training status
     */
    public TrainingStatus getSummaryStatusPublic() {
        return this.summaryStatusPublic;
    }

    public String getAiid() {
        return this.aiid;
    }

    public String getClient_token() {
        return this.clientToken;
    }

    public TrainingStatus getSummaryAiStatus() {
        return this.summaryStatusReal;
    }

    public void setPublishingState(final AiBot.PublishingState publishingState) {
        this.publishingState = publishingState;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(final double confidence) {
        this.confidence = confidence;
    }

    public BackendStatus getBackendStatus() {
        return this.backendStatus;
    }

    public void setBackendStatus(final BackendStatus backendStatus) {
        this.backendStatus = backendStatus;
        populateExtendedStatus();
    }

    public String getPassthroughUrl() {
        return this.passthroughUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean getIsPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(final boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getPersonality() {
        return this.personality;
    }

    public void setPersonality(final int personality) {
        this.personality = personality;
    }

    public int getVoice() {
        return this.voice;
    }

    public void setVoice(final int voice) {
        this.voice = voice;
    }

    public Locale getLanguage() {
        return this.language;
    }

    public void setLanguage(final Locale language) {
        this.language = language;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public boolean trainingFileUploaded() {
        return this.trainingFileUploaded;
    }

    public List<String> getDefaultChatResponses() {
        return this.defaultChatResponses;
    }

    public void setDefaultChatResponses(final List<String> defaultChatResponses) {
        this.defaultChatResponses = defaultChatResponses;
    }

    public boolean isReadOnly() {
        return this.readonly;
    }

    public void setReadOnly(final boolean isReadOnly) {
        this.readonly = isReadOnly;
    }

    public int getHandoverResetTimeoutSeconds() {
        return this.handoverResetTimeoutSeconds;
    }

    public int getErrorThresholdHandover() {
        return this.errorThresholdHandover;
    }

    public String getHandoverMessage() {
        return this.handoverMessage;
    }

    public void setHandoverResetTimeoutSeconds(final int handoverResetTimeoutSeconds) {
        this.handoverResetTimeoutSeconds = handoverResetTimeoutSeconds;
    }

    public void setErrorThresholdHandover(final int errorThresholdHandover) {
        this.errorThresholdHandover = errorThresholdHandover;
    }

    public void setHandoverMessage(final String handoverMessage) {
        this.handoverMessage = handoverMessage;
    }

    public void setAiid(final UUID aiid) {
        this.aiid = aiid.toString();
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setEngineVersion(final String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getEngineVersion() {
        return this.engineVersion;
    }

    private void populateExtendedStatus() {

        // unless we have servers online, this bot cannot be chatted with
        this.canChat = false;

        if (this.backendStatus == null) {
            this.summaryStatusReal = TrainingStatus.AI_UNDEFINED;
            this.phase1Progress = 0.0d;
        } else {
            BackendEngineStatus emb = this.backendStatus.getEngineStatus(BackendServerType.EMB);
            TrainingStatus embStatus = emb.getTrainingStatus();
            this.phase1Progress = clampProgress(embStatus, emb.getTrainingProgress());
            this.summaryStatusReal = getSummaryTrainingStatus(embStatus);
            this.canChat = UITrainingState.canChat(embStatus);

            BackendEngineStatus doc2chat = this.backendStatus.getEngineStatus(BackendServerType.DOC2CHAT);
            TrainingStatus doc2chatStatus = doc2chat.getTrainingStatus();
            if (doc2chatStatus == TrainingStatus.AI_TRAINING) {
                this.phase1Progress += clampProgress(doc2chatStatus, doc2chat.getTrainingProgress());
                this.phase1Progress /= 2;
                this.canChat = this.canChat && UITrainingState.canChat(doc2chatStatus);
                this.summaryStatusReal = TrainingStatus.AI_TRAINING;
            }
        }

        this.summaryStatusPublic = this.summaryStatusReal;

        // if the bot has been requeued, i.e. training started but paused for any reason
        // then we hide the pause by reporting this bot as "training..."
        if (this.summaryStatusReal == TrainingStatus.AI_TRAINING_QUEUED) {
            this.summaryStatusPublic = TrainingStatus.AI_TRAINING;
        }

        this.uiTrainingState = new UITrainingState(
                // use the summary status to generate a uistatus
                this.summaryStatusPublic,
                // EMB accounts for the 100% of training time
                this.phase1Progress,
                // placeholder error until we can actually report the real training error
                "an error has occurred");

    }

    /***
     * Report a progress value that is consistent
     * with the status we are reporting
     * @param status
     * @param reportedTrainingProgress
     * @return
     */
    private double clampProgress(final TrainingStatus status, final double reportedTrainingProgress) {
        double clampedProgress;
        switch (status) {
            case AI_UNDEFINED:
            case AI_READY_TO_TRAIN:
                clampedProgress = 0.0d;
                break;
            case AI_TRAINING_COMPLETE:
                clampedProgress = 1.0d;
                break;
            default:
                clampedProgress = reportedTrainingProgress;
                break;
        }
        // ensure that reported value can only be in the range 0.0d to 1.0d
        return Math.min(Math.max(clampedProgress, 0.0d), 1.0d);
    }

}
