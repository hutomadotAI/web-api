package com.hutoma.api.connectors;

import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.ai;

import java.util.ArrayList;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

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
}
