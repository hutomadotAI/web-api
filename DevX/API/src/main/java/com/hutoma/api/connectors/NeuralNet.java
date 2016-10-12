package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by David MG on 08/08/2016.
 */
public class NeuralNet {

    static long POLLEVERY = 1000;   // hard-coded to one second
    private static final String LOGFROM = "neuralnetconnector";
    private Database database;
    private MessageQueue messageQueue;
    private Logger logger;
    private Config config;
    private Tools tools;
    private long startTime;
    private long qid = 0;

    @Inject
    public NeuralNet(Database database, MessageQueue messageQueue, Logger logger, Config config, Tools tools) {
        this.database = database;
        this.messageQueue = messageQueue;
        this.logger = logger;
        this.config = config;
        this.tools = tools;
    }

    // Neural Network Query
    public void startAnswerRequest(String dev_id, UUID aiid, UUID chatId, String q) throws NeuralNetException {

        this.startTime = this.tools.getTimestamp();

        // if the RNN network is not active, then push a message to get it activated
        try {
            if (!this.database.isNeuralNetworkServerActive(dev_id, aiid)) {
                this.messageQueue.pushMessageStartRNN(dev_id, aiid);
            }
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "failed to check/start server " + e.toString());
            throw new NeuralNetException(e);
        }

        try {
            this.qid = this.database.insertNeuralNetworkQuestion(dev_id, chatId, aiid, q);
        } catch (Database.DatabaseException e) {
            throw new NeuralNetException(e);
        }

        // if less than zero then an error has occurred
        if (this.qid < 0) {
            throw new NeuralNetException(new Exception("negative qid"));
        }
    }

    public String getAnswerResult() throws NeuralNetException {

        String answer = "";

        // timeout in seconds
        long timeout = this.config.getNeuralNetworkTimeout();
        // calculate the exact time that we give up
        long endTime = this.startTime + (timeout * 1000);

        long timeNow;
        long timeRemaining;
        try {
            do {
                // do we have an answer?
                answer = this.database.getAnswer(this.qid);
                if ((null != answer) && (!answer.isEmpty())) {
                    // yes, end and return
                    return answer;
                }
                timeNow = this.tools.getTimestamp();
                timeRemaining = Math.max(0, endTime - timeNow);
                this.tools.threadSleep(Math.min(POLLEVERY, timeRemaining));
            } while (timeRemaining > 0);
        } catch (Database.DatabaseException dbe) {
            throw new NeuralNetException(dbe);
        }

        // otherwise no response appeared in a reasonable amount of time
        throw new NeuralNetNotRespondingException();
    }

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
}
