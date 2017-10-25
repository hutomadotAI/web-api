package com.hutoma.api.connectors.db;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.containers.sub.Integration;
import com.hutoma.api.containers.sub.IntegrationRecord;
import com.hutoma.api.containers.sub.IntegrationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseIntegrations extends Database {

    private static final String FACEBOOK_DEACTIVATED_MESSAGE =
            "Deactivated because another bot was integrated with this Facebook page.";


    @Inject
    public DatabaseIntegrations(final ILogger logger, final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    public List<Integration> getAiIntegrationList() throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntegrations", 0);
            ResultSet rs = call.executeQuery();
            List<Integration> list = new ArrayList<>();
            try {
                while (rs.next()) {
                    list.add(
                            new Integration(
                                    rs.getInt("int_id"),
                                    rs.getString("name"),
                                    rs.getString("description"),
                                    rs.getString("icon"),
                                    rs.getBoolean("available")));

                }
                return list;
            } catch (SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }



    public IntegrationRecord getIntegrationResource(final IntegrationType integration,
                                                    final String integratedResource)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getIntegratedResource", 2)
                    .add(integration.value())
                    .add(integratedResource);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return new IntegrationRecord(
                        UUID.fromString(rs.getString("aiid")),
                        UUID.fromString(rs.getString("devid")),
                        rs.getString("integrated_userid"),
                        rs.getString("data"),
                        rs.getString("status"),
                        rs.getBoolean("active"));
            }
            return null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public void updateIntegrationStatus(final UUID aiid, final IntegrationType integration,
                                        final String status, boolean setChatTimeNow)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateIntegrationStatus", 4)
                    .add(aiid)
                    .add(integration.value())
                    .add(status)
                    .add(setChatTimeNow);
            call.executeUpdate();
        }
    }

    public void deleteIntegration(final UUID aiid, final UUID devid,
                                  final IntegrationType integrationType) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("deleteIntegration", 3)
                    .add(aiid)
                    .add(devid)
                    .add(integrationType.value());
            call.executeUpdate();
        }
    }

    public boolean isIntegratedUserAlreadyRegistered(final IntegrationType integration, final String userID,
                                                     final UUID devid)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("checkIntegrationUser", 3)
                    .add(integration.value())
                    .add(userID)
                    .add(devid);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt("use_count") > 0;
            }
            return false;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    /***
     * Load the integration record without making any changes
     * @param aiid
     * @param devid
     * @param integration
     * @return
     * @throws DatabaseException
     */
    public IntegrationRecord getIntegration(final UUID aiid, final UUID devid,
                                            final IntegrationType integration) throws DatabaseException {
        final IntegrationRecord[] finalRecord = {null};

        // load the record but do not update it
        updateIntegrationRecord(aiid, devid, integration, (record) -> {
            finalRecord[0] = record;
            return null;
        });
        return finalRecord[0];
    }

    /***
     * Load the integration message, make changes in a lambda and save the changes,
     * all inside a transaction
     * @param aiid
     * @param devid
     * @param integration
     * @param updater
     * @return
     * @throws DatabaseException
     */
    public IntegrationRecord updateIntegrationRecord(final UUID aiid, final UUID devid,
                                                     final IntegrationType integration,
                                                     final UnaryOperator<IntegrationRecord> updater)
            throws DatabaseException {

        // open a transaction
        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            // load the current integration record
            ResultSet rs = transaction.getDatabaseCall()
                    .initialise("getAiIntegrationForUpdate", 3)
                    .add(aiid)
                    .add(devid)
                    .add(integration.value())
                    .executeQuery();
            IntegrationRecord record = rs.next() ? new IntegrationRecord(
                    rs.getString("integrated_resource"),
                    rs.getString("integrated_userid"),
                    rs.getString("data"),
                    rs.getString("status"),
                    rs.getBoolean("active"))
                    : null;

            // make the changes we require
            record = updater.apply(record);

            // if the updater still wants to save the result ...
            if (record != null) {

                // write the changes to the database
                DatabaseCall call = transaction.getDatabaseCall()
                        .initialise("updateAiIntegration", 9)
                        .add(aiid)
                        .add(devid)
                        .add(integration.value())
                        .add(record.getIntegrationResource())
                        .add(record.getIntegrationUserid())
                        .add(record.getData())
                        .add(record.getStatus())
                        .add(record.isActive())
                        .add(FACEBOOK_DEACTIVATED_MESSAGE);

                if (call.executeUpdate() > 0) {
                    transaction.commit();
                    return record;
                }
            } else {
                // this was a read-only transaction so
                // we can commit to release the locks now
                transaction.commit();
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
        return null;
    }

}
