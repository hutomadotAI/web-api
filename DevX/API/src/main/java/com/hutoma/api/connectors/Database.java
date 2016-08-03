package com.hutoma.api.connectors;

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



}
