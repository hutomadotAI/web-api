package com.hutoma.api.connectors;

import com.hutoma.api.common.Logger;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiMemoryToken;
import com.hutoma.api.containers.sub.AiDomain;
import com.hutoma.api.containers.sub.AiIntegration;
import com.hutoma.api.containers.sub.RateLimitStatus;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    private final String LOGFROM = "database";

    public static class DatabaseException extends Exception {
        public DatabaseException(Throwable cause) {
            super(cause);
        }
    }

    Logger logger;
    Provider<DatabaseCall> callProvider;

    @Inject
    public Database(Logger logger, Provider<DatabaseCall> callProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
    }


    public boolean createDev(String username, String email, String password, String passwordSalt, String first_name, String last_name, String dev_token, int plan_id, String dev_id, String client_token) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("addUser", 10).add(username).add(email).add(password).add(passwordSalt).add(first_name).add(last_name).add(dev_token).add(plan_id).add(dev_id).add(client_token);
            return call.executeUpdate() > 0;
        }
    }

    public boolean deleteDev(String devid) throws DatabaseException {

        //TODO: make this a single stored procedure
        // first delete all the user's AIs
        try (DatabaseCall deleleAICall = callProvider.get()) {
            deleleAICall.initialise("deleteAllAIs", 1).add(devid);
            deleleAICall.executeUpdate();

            // then delete the user
            try (DatabaseCall deleteUserCall = callProvider.get()) {
                deleteUserCall.initialise("deleteUser", 1).add(devid);
                return deleteUserCall.executeUpdate() > 0;
            }
        }
    }

    public boolean createAI(UUID aiid, String name, String description, String devid,
                            boolean is_private, double deep_learning_error, int deep_learning_status,
                            int shallow_learning_status, int status, String client_token, String trainingFile) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("addAI", 11)
                    .add(aiid).add(name).add(description).add(devid).add(is_private)
                    .add(deep_learning_error).add(deep_learning_status).add(shallow_learning_status)
                    .add(status).add(client_token).add(trainingFile);
            return call.executeUpdate() > 0;
        }
    }

    public ArrayList<ApiAi> getAllAIs(String devid) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("getAIs", 1).add(devid);
            ResultSet rs = call.executeQuery();
            ArrayList<ApiAi> res = new ArrayList<>();
            try {
                while (rs.next()) {
                    ApiAi ai = new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                            new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                            null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
                    res.add(ai);
                }
                return res;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public ApiAi getAI(String devid, UUID aiid) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("getAI_v1", 2).add(devid).add(aiid);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new ApiAi(rs.getString("aiid"), rs.getString("client_token"), rs.getString("ai_name"), rs.getString("ai_description"),
                            new DateTime(rs.getDate("created_on")), rs.getBoolean("is_private"), rs.getDouble("deep_learning_error"),
                            null, rs.getString("deep_learning_status"), rs.getString("ai_status"), null);
                }
                return null;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean deleteAi(String devid, UUID aiid) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("deleteAI_v1", 2).add(devid).add(aiid);
            return call.executeUpdate() > 0;
        }
    }

    public ArrayList<AiDomain> getAiDomainList() throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("getDomains", 0);
            ResultSet rs = call.executeQuery();
            try {
                ArrayList<AiDomain> res = new ArrayList<>();
                while (rs.next()) {
                    res.add(new AiDomain(rs.getString("dom_id"), rs.getString("name"), rs.getString("description"),
                            rs.getString("icon"), rs.getString("color"), rs.getBoolean("available")));
                }
                return res;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean isNeuralNetworkServerActive(String dev_id, UUID aiid) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("getAiActive", 2).add(aiid).add(dev_id);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return (1 == rs.getInt("NNActive"));
                }
                return false;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public long insertNeuralNetworkQuestion(String dev_id, UUID chatId, UUID aiid, String q) throws DatabaseException {

        try (DatabaseCall call = callProvider.get()) {
            call.initialise("insertQuestion", 4).add(dev_id).add(chatId).add(aiid).add(q);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return -1;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public String getAnswer(long qid) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("getAnswer", 1).add(qid);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString("answer");
                }
                return "";
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean updateAiTrainingFile(UUID aiUUID, String trainingData) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("updateTrainingData", 2).add(aiUUID).add(trainingData);
            return call.executeUpdate() > 0;
        }
    }

    public RateLimitStatus checkRateLimit(String dev_id, String rateKey, double burst, double frequency) throws DatabaseException {
        try (DatabaseCall call = callProvider.get()) {
            call.initialise("rate_limit_check", 4).add(dev_id).add(rateKey).add(burst).add(frequency);
            ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(rs.getBoolean("rate_limit"), rs.getFloat("tokens"));
                }
                throw new DatabaseException(new Exception("stored proc should have returned a row but it returned none"));
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<ApiMemoryToken> getAllUserVariables(String dev_id, String aiid, String uid) throws DatabaseException {
        try {
            return null; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public ApiMemoryToken getUserVariable(String dev_id, String aiid, String uid, String variable) throws DatabaseException {
        try {
            return null; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean setUserVariable(String dev_id, String aiid, String uid, int expires_seconds, int n_prompt,
                                   String variable_type, String variable_name, String variable_value) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

    }

    public boolean removeVariable(String dev_id, String aiid, String uid, String variable) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllUserVariables(String dev_id, String aiid, String uid) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public boolean removeAllAiVariables(String dev_id, String aiid) throws DatabaseException {
        try {
            return false; // FIXME
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /***
     * Temporarily fix build break
     * @return
     */
    public List<AiIntegration> getAiIntegrationList() throws DatabaseException {
        throw new DatabaseException(new Exception("getAiIntegrationList unimplemented"));
    }


}
