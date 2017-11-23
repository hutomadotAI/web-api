package com.hutoma.api.connectors.db;

import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.DeveloperInfo;

import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseMarketplace extends Database {

    @Inject
    public DatabaseMarketplace(final ILogger logger, final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
    }

    /**
     * Gets the developer info.
     * @param devId the developer id
     * @return the developer info
     * @throws DatabaseException
     */
    public DeveloperInfo getDeveloperInfo(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getDeveloperInfo", 1).add(devId);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                final String devIdString = rs.getString("dev_id");
                final UUID devIdDb = UUID.fromString(devIdString);
                return new DeveloperInfo(
                        devIdDb,
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("post_code"),
                        rs.getString("city"),
                        rs.getString("country"),
                        rs.getString("website")
                );
            }
            return null;
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    /**
     * Sets the developer info.
     * @param info the developer info
     * @return whether it performed the update or not
     * @throws DatabaseException
     */
    public boolean setDeveloperInfo(final DeveloperInfo info) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("setDeveloperInfo", 9)
                    .add(info.getDevId())
                    .add(info.getName())
                    .add(info.getCompany())
                    .add(info.getEmail())
                    .add(info.getAddress())
                    .add(info.getPostCode())
                    .add(info.getCity())
                    .add(info.getCountry())
                    .add(info.getWebsite());
            return call.executeUpdate() > 0;
        }
    }

    public String getBotIconPath(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotIcon", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getString("botIcon");
            }
            return null;
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean saveBotIconPath(final UUID devId, final int botId, final String filename)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("saveBotIcon", 3).add(devId).add(botId).add(filename);
            return call.executeUpdate() > 0;
        }
    }

    public boolean hasBotBeenPurchased(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("hasBotBeenPurchased", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) != 0;
            }
        } catch (final SQLException sqle) {
            throw new DatabaseException(sqle);
        }
        return false;
    }

    public AiBot getBotDetails(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotDetails", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            return rs.next() ? getAiBotFromResultset(rs) : null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public int publishBot(final AiBot bot, final DatabaseTransaction transaction) throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("publishBot", 18)
                    .add(bot.getDevId())
                    .add(bot.getAiid())
                    .add(bot.getName())
                    .add(bot.getDescription())
                    .add(bot.getLongDescription())
                    .add(bot.getAlertMessage())
                    .add(bot.getBadge())
                    .add(bot.getPrice())
                    .add(bot.getSample())
                    .add(bot.getLastUpdate())
                    .add(bot.getCategory())
                    .add(bot.getLicenseType())
                    .add(bot.getPrivacyPolicy())
                    .add(bot.getClassification())
                    .add(bot.getVersion())
                    .add(bot.getVideoLink())
                    .add(bot.getPublishingState().value())
                    .add(bot.getPublishingType().value());
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DatabaseException("Stored procedure publishBot did not return any value!");
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public boolean addBotTemplate(final int botId, final BotStructure botStructure,
                                  final DatabaseTransaction transaction,
                                  final JsonSerializer jsonSerializer)
            throws DatabaseException {
        try (DatabaseCall call = transaction == null ? this.callProvider.get() : transaction.getDatabaseCall()) {
            call.initialise("addBotTemplate", 2)
                    .add(botId)
                    .add(jsonSerializer.serialize(botStructure));
            return call.executeUpdate() > 0;
        }
    }

    public String getBotTemplate(final int botId)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotTemplate", 1).add(botId);
            final ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return rs.getString("template");
            }
            return null;
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }

    public AiBot getPublishedBotForAI(final UUID devId, final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPublishedBotForAi", 2).add(devId).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {
                return rs.next() ? getAiBotFromResultset(rs) : null;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public int getPublishedBotIdForAI(final UUID aiid) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPublishedBotIdForAi", 1).add(aiid);
            final ResultSet rs = call.executeQuery();
            try {

                return rs.next() ? rs.getInt("id") : -1;
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<AiBot> getPublishedBots(final AiBot.PublishingType publishingType) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPublishedBots", 1)
                    .add(publishingType.value());
            final ResultSet rs = call.executeQuery();
            try {
                return getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public List<AiBot> getPurchasedBots(final UUID devId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getPurchasedBots", 1).add(devId);
            final ResultSet rs = call.executeQuery();
            try {
                return getBotListFromResultset(rs);
            } catch (final SQLException sqle) {
                throw new DatabaseException(sqle);
            }
        }
    }

    public boolean purchaseBot(final UUID devId, final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("purchaseBot", 2).add(devId).add(botId);
            return call.executeUpdate() > 0;
        }
    }

    public boolean updateBotPublishingState(final int botId, final AiBot.PublishingState state)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("updateBotPublishingState", 2)
                    .add(botId)
                    .add(state.value());
            return call.executeUpdate() > 0;
        }
    }

    static AiBot getAiBotFromResultset(final ResultSet rs) throws SQLException {
        return new AiBot(
                UUID.fromString(rs.getString("dev_id")),
                UUID.fromString(rs.getString("aiid")),
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("long_description"),
                rs.getString("alert_message"),
                rs.getString("badge"),
                rs.getBigDecimal("price"),
                rs.getString("sample"),
                rs.getString("category"),
                rs.getString("license_type"),
                new DateTime(rs.getTimestamp("last_update")),
                rs.getString("privacy_policy"),
                rs.getString("classification"),
                rs.getString("version"),
                rs.getString("video_link"),
                AiBot.PublishingState.from(rs.getInt("publishing_state")),
                AiBot.PublishingType.from(rs.getInt("publishing_type")),
                rs.getString("botIcon")
        );
    }

    static List<AiBot> getBotListFromResultset(final ResultSet rs) throws SQLException {
        final ArrayList<AiBot> bots = new ArrayList<>();
        while (rs.next()) {
            bots.add(getAiBotFromResultset(rs));
        }
        return bots;
    }
}
