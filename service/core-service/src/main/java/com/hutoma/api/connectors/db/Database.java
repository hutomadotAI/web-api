package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;
import com.hutoma.api.containers.sub.RateLimitStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by David MG on 02/08/2016.
 */
public class Database {

    protected static final String LOGFROM = "database";

    protected final ILogger logger;
    protected final Provider<DatabaseCall> callProvider;
    protected Provider<DatabaseTransaction> transactionProvider;

    @Inject
    public Database(final ILogger logger, final Provider<DatabaseCall> callProvider,
                    final Provider<DatabaseTransaction> transactionProvider) {
        this.logger = logger;
        this.callProvider = callProvider;
        this.transactionProvider = transactionProvider;
    }

    /***
     * Truncate strings if they are too long for the database field
     * @param field source data
     * @param maxLength
     * @return
     */
    static String limitSize(final String field, final int maxLength) {
        return ((field == null) || (field.length() <= maxLength)) ? field : field.substring(0, maxLength);
    }


    public RateLimitStatus checkRateLimit(final UUID devId, final String rateKey, final double burst,
                                          final double frequency) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("rateLimitCheck", 4)
                    .add(devId).add(rateKey).add(burst).add(frequency);
            final ResultSet rs = call.executeQuery();
            try {
                if (rs.next()) {
                    return new RateLimitStatus(
                            rs.getBoolean("rate_limit"),
                            rs.getFloat("tokens"),
                            rs.getBoolean("valid"));
                }
                throw new DatabaseException(
                        new Exception("stored proc should have returned a row but it returned none"));
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }
}
