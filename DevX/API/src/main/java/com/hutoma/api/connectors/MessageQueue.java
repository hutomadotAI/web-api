package com.hutoma.api.connectors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.hutoma.api.common.Config;


public class MessageQueue {

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

    public boolean pushMessageDeleteDev(Config config, String devid) {
        return pushMessage(config, AwsMessage.delete_dev + "|" + devid + "|000");
    }

    public boolean pushMessageDeleteAI(Config config, String devid, String aiid) {
        return pushMessage(config, AwsMessage.delete_ai + "|" + devid + "|" + aiid);
    }

    protected boolean pushMessage(Config config, String message) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {return false;}

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region targetRegion = Region.getRegion(config.getMessageQueueRegion());
        sqs.setRegion(targetRegion);

        try {
            sqs.sendMessage(new SendMessageRequest(config.getCoreQueue(), message));
        }  catch (Exception e) {
            System.out.print(e.getMessage());
           return  false;
        }

        return true;
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



