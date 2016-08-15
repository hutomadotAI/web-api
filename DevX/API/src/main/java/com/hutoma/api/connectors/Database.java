package com.hutoma.api.connectors;

import com.hutoma.api.common.Logger;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.ai;
import hutoma.api.server.db.domain;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * Created by David MG on 02/08/2016.
 * This is a single class entry point for all database calls
 * for future refactoring of database connectivity
 */
public class Database {

    private final String LOGFROM = "database";

    public class DatabaseException extends Exception {
        public DatabaseException(Throwable cause) {
            super(cause);
        }
    }

    Logger logger;

    @Inject
    public Database(Logger logger) {
        this.logger = logger;
    }

    public boolean createDev(String username, String email, String password, String passwordSalt, String name, String attempt, String dev_token, int planId, String devid) {
        return hutoma.api.server.db.ai.create_dev(username, email, password, passwordSalt, name, attempt, dev_token, planId, devid);
    }

    public boolean deleteDev(String devid) {
        return hutoma.api.server.db.dev.delete_dev(devid);
    }

    public boolean createAI(String aiid, String name, String description, String devid,
                            boolean is_private, double deep_learning_error, int deep_learning_status,
                            int shallow_learning_status, int status, String client_token, String trainingFile) {
        return hutoma.api.server.db.ai.create_ai(aiid, name, description, devid,
                is_private, deep_learning_error, deep_learning_status,
                shallow_learning_status, status, client_token, trainingFile);
    }

    public ArrayList<api_root._ai> getAllAIs(String devid) {
        return hutoma.api.server.db.ai.get_all_ai(devid);
    }

    public api_root._ai getAI(String aiid) {
        return hutoma.api.server.db.ai.get_ai(aiid);
    }

    public boolean deleteAi(String aiid) {
        return hutoma.api.server.db.ai.delete_ai(aiid);
    }

    public ArrayList<api_root._domain> getAllDomains() {
        return hutoma.api.server.db.domain.get_all_domains();
    }

    public boolean isNeuralNetworkServerActive(String dev_id, String aiid) throws Exception {
        try {
            return hutoma.api.server.db.RNN.is_RNN_active(dev_id, aiid);
        } catch (Exception e) {
            logger.logError(LOGFROM, "db call failed: " + e.toString());
            throw e;
        }
    }

    public long insertNeuralNetworkQuestion(String dev_id, String uid, String aiid, String q) {
        try {
            return hutoma.api.server.db.RNN.insertQuestion(dev_id, uid, aiid, q);
        } catch (Exception e) {
            logger.logError(LOGFROM, "db call failed: " + e.toString());
            return -1;
        }
    }

    public String getAnswer(long qid) {
        try {
            return hutoma.api.server.db.RNN.getAnswer(qid);
        } catch (Exception e) {
            logger.logError(LOGFROM, "db call failed: " + e.toString());
        }
        return null;
    }

    public void updateAiTrainingFile(String aiid, String trainingData) throws DatabaseException {
        try {
            ai.update_ai_training_file(aiid, trainingData);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

}
