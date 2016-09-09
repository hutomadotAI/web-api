package com.hutoma.api.connectors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;

import javax.inject.Inject;


public class MessageQueue {

    public static class MessageQueueException extends Exception {

        public MessageQueueException(Throwable cause) {
            super(cause);
        }
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
        internal_error,
        malformed_training_file,
        training_queued,
        training_in_progress,
        training_in_progress_rnnavailable,
        training_stopped_maxtime,
        training_completed,
        start_RNN
    }

    private final String LOGFROM = "messagequeue";

    Config config;
    Logger logger;

    @Inject
    public MessageQueue(Config config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void pushMessageDeleteDev(String devid) throws MessageQueueException {
        pushMessage(AwsMessage.delete_dev + "|" + devid + "|000");
    }

    public void pushMessageDeleteAI(String devid, String aiid) throws MessageQueueException {
        pushMessage(AwsMessage.delete_ai + "|" + devid + "|" + aiid);
    }

    public void pushMessageStartRNN(String devid, String aiid) throws MessageQueueException {
        pushMessage(AwsMessage.start_RNN + "|" + devid + "|" + aiid);
    }

    public void pushMessageReadyForTraining(String devid, String aiid) throws MessageQueueException {
        pushMessage(AwsMessage.ready_for_training + "|" + devid + "|" + aiid);
    }

    public void pushMessagePreprocessTrainingText(String devid, String aiid) throws MessageQueueException {
        pushMessage(AwsMessage.preprocess_training_text + "|" + devid + "|" + aiid);
    }

    public void pushMessageClusterSplit(String devid, String aiid, double clusterMinProbability) throws MessageQueueException {
        pushMessage(AwsMessage.cluster_split + "|" + devid + "|" + aiid + "|" + clusterMinProbability);
    }

    public void pushMessageDeleteTraining(String devid, String aiid) throws MessageQueueException {
        pushMessage(AwsMessage.delete_training + "|" + devid + "|" + aiid);
    }

    protected void pushMessage(String message) throws MessageQueueException {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            logger.logError(LOGFROM, "getCredentials error " + e.toString());
            throw new MessageQueueException(e);
        }

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region targetRegion = Region.getRegion(config.getMessageQueueRegion());
        sqs.setRegion(targetRegion);

        try {
            sqs.sendMessage(new SendMessageRequest(config.getCoreQueue(), message));
        } catch (Exception e) {
            logger.logError(LOGFROM, "sendMessage error " + e.toString());
            throw new MessageQueueException(e);
        }
    }

    @Deprecated
    public static boolean push_msg(String queue, String message) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {return false;}

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usWest2);

        try {
            sqs.sendMessage(new SendMessageRequest(queue, message));

        }  catch (Exception e) {
            System.out.print(e.getMessage());
            return  false;
        }

        return true;
    }
}



