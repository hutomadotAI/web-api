package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;

import javax.inject.Inject;

/**
 * Created by David MG on 08/08/2016.
 */
public class NeuralNet {

    Database database;
    MessageQueue messageQueue;
    Logger logger;
    Config config;
    Tools tools;

    private final String LOGFROM = "neuralnetconnector";
    static long POLLEVERY = 1000;                                  // hard-coded to one second

    public static class NeuralNetNotRespondingException extends Exception {
    }

    @Inject
    public NeuralNet(Database database, MessageQueue messageQueue, Logger logger, Config config, Tools tools) {
        this.database = database;
        this.messageQueue = messageQueue;
        this.logger = logger;
        this.config = config;
        this.tools = tools;
    }

    // Neural Network Query
    public String getAnswer(String dev_id, String aiid, String uid, String q) throws NeuralNetNotRespondingException {

        // if the RNN network is not active, then push a message to get it activated
        try {
            if (!database.isNeuralNetworkServerActive(dev_id, aiid)) {

                //TODO: fix this
                try {
                    messageQueue.pushMessageStartRNN(dev_id, aiid);
                } catch (MessageQueue.MessageQueueException mqe) {
                    throw new Exception("failed to send message to message queue");
                }
            }
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to check/start server " + e.toString());
            return null;
        }

        String answer = "";

        long qid = database.insertNeuralNetworkQuestion(dev_id, uid, aiid, q);

        // if less than zero then an error has occurred
        if (qid<0) {
            return null;
        }

        // TODO: Polling loop: find a better way
        long timeout = config.getNeuralNetworkTimeout();
        long startTime = tools.getTimestamp();
        long endTime = startTime + (timeout * 1000);
        do {
            tools.threadSleep(POLLEVERY);
            answer = database.getAnswer(qid);
            if (null==answer) {
                // an error has occurred, so exit now
                return null;
            } else {
                if (!answer.isEmpty()) {
                    // we have an answer!
                    return answer;
                }
            }
        } while ((tools.getTimestamp() + (POLLEVERY)) < endTime);

        // otherwise no response appeared in a reasonable amount of time
        throw new NeuralNetNotRespondingException();
    }
}
