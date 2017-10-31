package com.hutoma.api.containers.sub;

/***
 * Result of a question being correctly sent to the RNN
 * Has the question been accepted? Was the AI in a good state? What is the question ID?
 */
public class ChatRequestStatus {

    private long questionId;
    private TrainingStatus aiStatus;
    private boolean questionRejectedDueToAiStatus;

    public ChatRequestStatus(long questionId, TrainingStatus aiStatus, boolean questionRejectedDueToAiStatus) {
        this.questionId = questionId;
        this.aiStatus = aiStatus;
        this.questionRejectedDueToAiStatus = questionRejectedDueToAiStatus;
    }

    /***
     * Complete failure case
     */
    public ChatRequestStatus() {
        this.questionId = -1;
        this.aiStatus = null;
        this.questionRejectedDueToAiStatus = true;
    }

    public long getQuestionId() {
        return this.questionId;
    }

    public boolean isQuestionRejectedDueToAiStatus() {
        return this.questionRejectedDueToAiStatus;
    }

    public TrainingStatus getAiStatus() {
        return this.aiStatus;
    }
}
