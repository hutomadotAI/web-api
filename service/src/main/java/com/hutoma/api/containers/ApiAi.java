package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;

import java.util.Locale;

/**
 * Created by David MG on 15/08/2016.
 */
public class ApiAi extends ApiResult {

    private final String aiid;
    @SerializedName("client_token")
    private final String clientToken;

    private String name;
    private String description;

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

    // this is a single status that represents the ai state on multiple backend servers
    @SerializedName("ai_status")
    private TrainingStatus summaryStatus;

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

    public ApiAi(String aiid, String clientToken) {
        this.aiid = aiid;
        this.clientToken = clientToken;
    }

    public ApiAi(String aiid, String clientToken, String name, String description, DateTime createdOn,
                 boolean isPrivate, BackendStatus backendStatus, boolean hasTrainingFile,
                 int personality, double confidence, int voice, Locale language, String timezone) {
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

    public String getAiid() {
        return this.aiid;
    }

    public String getClient_token() {
        return this.clientToken;
    }

    public TrainingStatus getSummaryAiStatus() {
        return this.summaryStatus;
    }

    private void populateExtendedStatus() {
        if (this.backendStatus == null) {
            this.summaryStatus = TrainingStatus.AI_UNDEFINED;
            this.phase1Progress = 0.0d;
            this.phase2Progress = 0.0d;
        } else {
            BackendEngineStatus wnet = this.backendStatus.getEngineStatus(BackendServerType.WNET);
            BackendEngineStatus rnn = this.backendStatus.getEngineStatus(BackendServerType.RNN);

            TrainingStatus wnetStatus = wnet.getTrainingStatus();
            TrainingStatus rnnStatus = rnn.getTrainingStatus();

            this.summaryStatus = getSummaryTrainingStatus(wnetStatus, rnnStatus);
            this.phase1Progress = clampProgress(wnetStatus, wnet.getTrainingProgress());
            this.phase2Progress = clampProgress(rnnStatus, rnn.getTrainingProgress());
            this.deepLearningError = rnn.getTrainingError();
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
