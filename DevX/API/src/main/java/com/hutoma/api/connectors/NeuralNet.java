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

    public static class NeuralNetException extends Exception {
        public NeuralNetException(Throwable cause) {
            super(cause);
        }
    }

    public static class NeuralNetNotRespondingException extends NeuralNetException {
        public NeuralNetNotRespondingException() {
            super(new Exception("not responding"));
        }
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
    public String getAnswer(String dev_id, String aiid, String uid, String q) throws NeuralNetException {

        // if the RNN network is not active, then push a message to get it activated
        try {
            if (!database.isNeuralNetworkServerActive(dev_id, aiid)) {
                messageQueue.pushMessageStartRNN(dev_id, aiid);
            }
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to check/start server " + e.toString());
            throw new NeuralNetException(e);
        }

        String answer = "";

        long qid = 0;
        try {
            qid = database.insertNeuralNetworkQuestion(dev_id, uid, aiid, q);
        } catch (Database.DatabaseException e) {
            throw new NeuralNetException(e);
        }

        // if less than zero then an error has occurred
        if (qid<0) {
            return null;
        }

        // TODO: Polling loop: find a better way
        long timeout = config.getNeuralNetworkTimeout();
        long startTime = tools.getTimestamp();
        long endTime = startTime + (timeout * 1000);
        try {
            do {
                tools.threadSleep(POLLEVERY);
                answer = database.getAnswer(qid);
                if ((null!=answer) && (!answer.isEmpty())) {
                    // we have an answer!
                    return answer;
                }
            } while ((tools.getTimestamp() + (POLLEVERY)) < endTime);
        } catch (Database.DatabaseException dbe) {
            throw new NeuralNetException(dbe);
        }

        // otherwise no response appeared in a reasonable amount of time
        throw new NeuralNetNotRespondingException();
    }
}
