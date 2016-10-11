package com.hutoma.api.containers.sub;

/**
 * Created by mauriziocibelli on 11/10/16.
 */
public class TrainingStatus {

    public static enum trainingStatus {
        training_stopped,
        training_not_started,
        training_queued,
        training_in_progress,
        training_stopped_maxtime,
        training_completed,
        training_deleted
    }
}
