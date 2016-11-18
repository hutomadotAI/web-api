package com.hutoma.api.connectors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;

import java.util.UUID;
import javax.inject.Inject;


public class MessageQueue {

    private static final String LOGFROM = "messagequeue";
    private final Config config;
    private final ILogger logger;

    @Inject
    public MessageQueue(final Config config, final ILogger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void pushMessageDeleteDev(String devid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.delete_dev + "|" + devid + "|000");
    }

    public void pushMessageDeleteAI(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.delete_ai + "|" + devid + "|" + aiid.toString());
    }

    public void pushMessageStartRNN(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.start_RNN + "|" + devid + "|" + aiid.toString());
    }

    public void pushMessageReadyForTraining(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.ready_for_training + "|" + devid + "|" + aiid.toString());
    }

    public void pushMessagePreprocessTrainingText(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getQuestionGeneratorQueue(), AwsMessage.preprocess_training_text + "|" + devid + "|"
                + aiid.toString());
    }

    public void pushMessageClusterSplit(String devid, UUID aiid, double clusterMinProbability)
            throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.cluster_split + "|" + devid + "|" + aiid.toString() + "|"
                + clusterMinProbability);
    }

    public void pushMessageDeleteTraining(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.delete_training + "|" + devid + "|" + aiid.toString());
    }

    public void pushMessageStopTraining(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.stop_training + "|" + devid + "|" + aiid.toString());
    }

    public void pushMessageUpdateTraining(String devid, UUID aiid) throws MessageQueueException {
        pushMessage(this.config.getCoreQueue(), AwsMessage.update_training + "|" + devid + "|" + aiid.toString());
    }

    public enum AwsMessage {
        cluster_split,
        delete_dev,
        delete_ai,
        preprocess_training_text,
        preprocess_training_html,
        ready_for_training,
        stop_training,
        start_training,
        delete_training,
        update_training,
        internal_error,
        malformed_training_file,
        start_RNN
    }

    public static class MessageQueueException extends Exception {

        public MessageQueueException(Throwable cause) {
            super(cause);
        }
    }

    protected void pushMessage(String queue, String message) throws MessageQueueException {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "getCredentials error " + e.toString());
            throw new MessageQueueException(e);
        }

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region targetRegion = Region.getRegion(this.config.getMessageQueueRegion());
        sqs.setRegion(targetRegion);

        try {
            sqs.sendMessage(new SendMessageRequest(queue, message));
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "sendMessage error " + e.toString());
            throw new MessageQueueException(e);
        }
    }

}



