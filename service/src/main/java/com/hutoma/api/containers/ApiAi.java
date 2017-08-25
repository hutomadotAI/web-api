package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

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

    public ApiAi(String aiid, String clientToken) {
        this.aiid = aiid;
        this.clientToken = clientToken;
        this.hmacSecret = null;
    }

    public ApiAi(final String aiid, final String clientToken, final String name, final String description,
                 final DateTime createdOn, final boolean isPrivate, final BackendStatus backendStatus,
                 final boolean hasTrainingFile, final int personality, final double confidence, final int voice,
                 final Locale language, final String timezone, final String hmacSecret,
                 String passthroughUrl, final List<String> defaultChatResponses) {
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
        populateExtendedStatus();
    }

    /***
     * Reports "summary status" for both back-end servers by taking the one that is furthest behind.
     * @param wnetStatus
     * @param rnnStatus
     * @return
     */
    private static TrainingStatus getSummaryTrainingStatus(TrainingStatus wnetStatus, TrainingStatus rnnStatus) {
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

    public String getName() { return this.name; }

    public String getDescription() { return this.description; }

    public boolean trainingFileUploaded() {
        return this.trainingFileUploaded;
    }

    public List<String> getDefaultChatResponses() {
        return this.defaultChatResponses;
    }

    public void setDefaultChatResponses(final List<String> defaultChatResponses) {
        this.defaultChatResponses = defaultChatResponses;
    }

    private void populateExtendedStatus() {
        if (this.backendStatus == null) {
            this.summaryStatusReal = TrainingStatus.AI_UNDEFINED;
            this.phase1Progress = 0.0d;
            this.phase2Progress = 0.0d;
        } else {
            BackendEngineStatus wnet = this.backendStatus.getEngineStatus(BackendServerType.WNET);
            BackendEngineStatus rnn = this.backendStatus.getEngineStatus(BackendServerType.RNN);

            TrainingStatus wnetStatus = wnet.getTrainingStatus();
            TrainingStatus rnnStatus = rnn.getTrainingStatus();

            this.summaryStatusReal = getSummaryTrainingStatus(wnetStatus, rnnStatus);
            this.phase1Progress = clampProgress(wnetStatus, wnet.getTrainingProgress());
            this.phase2Progress = clampProgress(rnnStatus, rnn.getTrainingProgress());
            this.deepLearningError = rnn.getTrainingError();
        }

        // if the bot has been requeued, i.e. training started but paused for any reason
        if ((this.summaryStatusReal == TrainingStatus.AI_TRAINING_QUEUED) && this.phase2Progress > 0.001) {
            // then we hide the pause by reporting this bot as "training..."
            this.summaryStatusPublic = TrainingStatus.AI_TRAINING;
        } else {
            this.summaryStatusPublic = this.summaryStatusReal;
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
