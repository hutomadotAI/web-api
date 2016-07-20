package hutoma.api.server.AWS;

/**
 * Created by mauriziocibelli on 02/05/16.
 */
public enum msg {
    cluster_split,
    delete_dev,
    delete_ai,
    preprocess_training_text,
    preprocess_training_html,
    ready_for_training,
    stop_training,
    start_training,
    delete_training,
    internal_error,
    malformed_training_file,
    training_queued,
    training_in_progress,
    training_in_progress_rnnavailable,
    training_stopped_maxtime,
    training_completed,
    start_RNN

}
