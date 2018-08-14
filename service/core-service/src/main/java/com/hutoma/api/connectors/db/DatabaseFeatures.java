package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseFeatures extends Database {

    @Inject
    public DatabaseFeatures(final ILogger logger, final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    public List<DatabaseFeature> getAllFeatures() throws DatabaseException {

        List<DatabaseFeature> features = new ArrayList<>();
        try (DatabaseCall call = this.callProvider.get()) {
            ResultSet rs = call
                    .initialise("getFeatures", 0)
                    .executeQuery();
            while (rs.next()) {
                String value = rs.getString("devid");
                UUID devId = StringUtils.isEmpty(value) ? null : UUID.fromString(value);
                value = rs.getString("aiid");
                UUID aiid = StringUtils.isEmpty(value) ? null : UUID.fromString(value);
                DatabaseFeature feature = new DatabaseFeature(
                        devId,
                        aiid,
                        rs.getString("feature"),
                        rs.getString("state")
                );
                features.add(feature);
            }

        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
        return features;
    }

    public static class DatabaseFeature {
        private UUID devId;
        private UUID aiid;
        private String feature;
        private String state;

        public DatabaseFeature(final UUID devId, final UUID aiid, final String feature, final String state) {
            this.devId = devId;
            this.aiid = aiid;
            this.feature = feature;
            this.state = state;
        }

        public UUID getDevId() {
            return this.devId;
        }

        public UUID getAiid() {
            return this.aiid;
        }

        public String getFeature() {
            return this.feature.toUpperCase();
        }

        public String getState() {
            return this.state.toUpperCase();
        }
    }
}