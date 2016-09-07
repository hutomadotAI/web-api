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

    long startTime;
    long qid = 0;

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
    public void startAnswerRequest(String dev_id, String aiid, String uid, String q) throws NeuralNetException {

        startTime = tools.getTimestamp();

        // if the RNN network is not active, then push a message to get it activated
        try {
            if (!database.isNeuralNetworkServerActive(dev_id, aiid)) {
                messageQueue.pushMessageStartRNN(dev_id, aiid);
            }
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to check/start server " + e.toString());
            throw new NeuralNetException(e);
        }

        try {
            qid = database.insertNeuralNetworkQuestion(dev_id, uid, aiid, q);
        } catch (Database.DatabaseException e) {
            throw new NeuralNetException(e);
        }

        // if less than zero then an error has occurred
        if (qid < 0) {
            throw new NeuralNetException(new Exception("negative qid"));
        }
    }

    public String getAnswerResult() throws NeuralNetException {

        String answer = "";

        // timeout in seconds
        long timeout = config.getNeuralNetworkTimeout();
        // calculate the exact time that we give up
        long endTime = startTime + (timeout * 1000);

        long timeNow;
        long timeRemaining;
        try {
            do {
                // do we have an answer?
                answer = database.getAnswer(qid);
                if ((null!=answer) && (!answer.isEmpty())) {
                    // yes, end and return
                    return answer;
                }
                timeNow = tools.getTimestamp();
                timeRemaining = Math.max(0, endTime - timeNow);
                tools.threadSleep(Math.min(POLLEVERY, timeRemaining));
            } while (timeRemaining > 0);
        } catch (Database.DatabaseException dbe) {
            throw new NeuralNetException(dbe);
        }

        // otherwise no response appeared in a reasonable amount of time
        throw new NeuralNetNotRespondingException();
    }
}
