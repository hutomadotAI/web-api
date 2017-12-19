package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.containers.sub.UITrainingState;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ApiAi extends ApiResult {

    private final String aiid;
    @SerializedName("client_token")
    private final String clientToken;

    @SerializedName("hmac_secret")
    private final String hmacSecret;

    private String name;
    private String description;
    private String passthroughUrl;

    @SerializedName("created_on")
    private DateTime createdOn;

    @SerializedName("is_private")
    private boolean isPrivate;

    private int personality; // aka. Learn from users
    private double confidence; // aka Create new answers
    private int voice;
    private Locale language;
    private String timezone;
    // transient because this should never be serialized along with the ApiAi object
    private transient BackendStatus backendStatus;
    // a single real status that represents the ai state on multiple backend servers
    private transient TrainingStatus summaryStatusReal;
    // a public version of summaryStatus, with requeueing phases masked as "training"
    @SerializedName("ai_status")
    private TrainingStatus summaryStatusPublic;
    // value from 0.0 to 1.0 representing training progress on the WNET server
    @SerializedName("phase_1_progress")
    private double phase1Progress;
    // value from 0.0 to 1.0 representing training progress on the deep learning server
    @SerializedName("phase_2_progress")
    private double phase2Progress;
    // value from around 1.0 (100%) to 10000 or more (~0%)
    @SerializedName("deep_learning_error")
    private double deepLearningError;
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
        this.backendStatus = backendStatus;
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
        populateExtendedStatus();
    }

    public ApiAi(final ApiAi other) {
        this.aiid = other.aiid;
        this.clientToken = other.clientToken;
        this.name = other.name;
        this.description = other.description;
        this.createdOn = other.createdOn;
        this.isPrivate = other.isPrivate;
        this.backendStatus = other.backendStatus;
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
        populateExtendedStatus();
    }

    /***
     * Reports "summary status" for both back-end servers by taking the one that is furthest behind.
     * @param wnetStatus
     * @param rnnStatus
     * @return
     */
    private static TrainingStatus getSummaryTrainingStatus(TrainingStatus wnetStatus, TrainingStatus rnnStatus) {
        if (rnnStatus == null) {
            return wnetStatus;
        }

        if ((wnetStatus == TrainingStatus.AI_ERROR) || (rnnStatus == TrainingStatus.AI_ERROR)) {
            return TrainingStatus.AI_ERROR;
        }
        if ((wnetStatus == TrainingStatus.AI_UNDEFINED) || (rnnStatus == TrainingStatus.AI_UNDEFINED)) {
            return TrainingStatus.AI_UNDEFINED;
        }
        if ((wnetStatus == TrainingStatus.AI_READY_TO_TRAIN) || (rnnStatus == TrainingStatus.AI_READY_TO_TRAIN)) {
            return TrainingStatus.AI_READY_TO_TRAIN;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING_QUEUED) || (rnnStatus == TrainingStatus.AI_TRAINING_QUEUED)) {
            return TrainingStatus.AI_TRAINING_QUEUED;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING_STOPPED) || (rnnStatus == TrainingStatus.AI_TRAINING_STOPPED)) {
            return TrainingStatus.AI_TRAINING_STOPPED;
        }
        if ((wnetStatus == TrainingStatus.AI_TRAINING) || (rnnStatus == TrainingStatus.AI_TRAINING)) {
            return TrainingStatus.AI_TRAINING;
        }
        return TrainingStatus.AI_TRAINING_COMPLETE;
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
        if (!linkedBots.isEmpty()) {
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

    public int getPersonality() {
        return this.personality;
    }

    public int getVoice() {
        return this.voice;
    }

    public Locale getLanguage() {
        return this.language;
    }

    public String getTimezone() {
        return this.timezone;
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

    private void populateExtendedStatus() {
        boolean isRnnEnabled = false;

        // unless we have servers online, this bot cannot be chatted with
        this.canChat = false;
        this.phase2Progress = 0.0d;

        if (this.backendStatus == null) {
            this.summaryStatusReal = TrainingStatus.AI_UNDEFINED;
            this.phase1Progress = 0.0d;
        } else {
            BackendEngineStatus wnet = this.backendStatus.getEngineStatus(BackendServerType.WNET);
            TrainingStatus wnetStatus = wnet.getTrainingStatus();
            this.phase1Progress = clampProgress(wnetStatus, wnet.getTrainingProgress());

            TrainingStatus rnnStatus = null;
            isRnnEnabled = this.backendStatus.hasEngineStatus(BackendServerType.RNN);
            if (isRnnEnabled) {
                BackendEngineStatus rnn = this.backendStatus.getEngineStatus(BackendServerType.RNN);
                rnnStatus = rnn.getTrainingStatus();
                this.phase2Progress = clampProgress(rnnStatus, rnn.getTrainingProgress());
                this.deepLearningError = rnn.getTrainingError();
            }

            this.summaryStatusReal = getSummaryTrainingStatus(wnetStatus, rnnStatus);

            // depending on which servers are in what state for the bot,
            // we can determine whether we can chat with it or not
            this.canChat = UITrainingState.canChat(wnetStatus);
        }

        if (isRnnEnabled) {
            // if the bot has been requeued, i.e. training started but paused for any reason
            if ((this.summaryStatusReal == TrainingStatus.AI_TRAINING_QUEUED) && this.phase2Progress > 0.001) {
                // then we hide the pause by reporting this bot as "training..."
                this.summaryStatusPublic = TrainingStatus.AI_TRAINING;
            } else {
                this.summaryStatusPublic = this.summaryStatusReal;
            }

            this.uiTrainingState = new UITrainingState(
                    // use the summary status to generate a uistatus
                    this.summaryStatusPublic,
                    // the first 25% is for WNET, the rest is RNN progress
                    (this.phase1Progress * 0.25) + (this.phase2Progress * 0.75),
                    // placeholder error until we can actually report the real training error
                    "an error has occurred");
        } else {
            // Only WNET in use
            this.summaryStatusPublic = this.summaryStatusReal;
            this.uiTrainingState = new UITrainingState(
                    // use the summary status to generate a uistatus
                    this.summaryStatusPublic,
                    // WNET accounts for the 100% of training time
                    this.phase1Progress,
                    // placeholder error until we can actually report the real training error
                    "an error has occurred");
        }

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
