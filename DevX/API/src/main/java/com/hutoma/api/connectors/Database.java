package com.hutoma.api.connectors;

import com.hutoma.api.common.Logger;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiMemoryToken;
import hutoma.api.server.db.ai;
import hutoma.api.server.db.memory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David MG on 02/08/2016.
 * This is a single class entry point for all database calls
 * for future refactoring of database connectivity
 */
public class Database {

    private final String LOGFROM = "database";

    public static class DatabaseException extends Exception {
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

    public ArrayList<ApiAi> getAllAIs(String devid) {
        return hutoma.api.server.db.ai.get_all_ai(devid);
    }

    public ApiAi getAI(String aiid) {
        return hutoma.api.server.db.ai.get_ai(aiid);
    }

    public boolean deleteAi(String aiid) {
        return hutoma.api.server.db.ai.delete_ai(aiid);
    }

    public ArrayList<AiDomain> getAiDomainList() {
        return hutoma.api.server.db.domain.getAiDomainList();
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

    public List<ApiMemoryToken> getAllUserVariables(String dev_id, String aiid, String uid) throws DatabaseException {
        try {
            return memory.get_all_user_variables(dev_id, aiid, uid);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ApiMemoryToken getUserVariable(String dev_id, String aiid, String uid, String variable) throws DatabaseException {
        try {
            return memory.get_user_variable(dev_id, aiid, uid, variable);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean setUserVariable(String dev_id, String aiid, String uid, int expires_seconds, int n_prompt,
                                        String variable_type, String variable_name, String variable_value) throws DatabaseException {
        try {
            return memory.set_variable(dev_id, aiid, uid, expires_seconds, n_prompt, variable_type, variable_name, variable_value);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

    }

    public boolean removeVariable(String dev_id, String aiid, String uid, String variable) throws DatabaseException {
        try {
            return memory.remove_variable(dev_id, aiid, uid, variable);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllUserVariables(String dev_id, String aiid, String uid) throws DatabaseException {
        try {
            return memory.remove_all_user_variables(dev_id, aiid, uid);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllAiVariables(String dev_id, String aiid) throws DatabaseException {
        try {
            return memory.remove_all_ai_variables(dev_id, aiid);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

}
