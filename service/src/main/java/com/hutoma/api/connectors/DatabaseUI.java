package com.hutoma.api.connectors;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.db.DatabaseCall;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.containers.ui.ApiBotstoreCategoryItemList;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Database access for UI methods.
 */
public class DatabaseUI extends Database {

    protected static final String LOGFROM = "db ui";
    private final ILogger logger;

    @Inject
    public DatabaseUI(final ILogger logger,
                      final Provider<DatabaseCall> callProvider,
                      final Provider<DatabaseTransaction> transactionProvider) {
        super(logger, callProvider, transactionProvider);
        this.logger = logger;
    }

    public ApiBotstoreItemList getBotstoreList(final int startFrom, final int pageSize,
                                               final List<String> filterList,
                                               final String orderField, final String orderDirection)
            throws DatabaseException {
        List<BotstoreItem> list = new ArrayList<>();
        try (DatabaseCall call = this.callProvider.get()) {
            int totalResults = 0;
            int order = 0;

            String sortOrder = orderField.isEmpty() || orderDirection.isEmpty()
                    ? "" : String.format("%s %s", orderField, orderDirection);
            String filters = (filterList == null || filterList.isEmpty())
                    ? "" : String.join(" AND ", filterList);
            call.initialise("getBotstoreList", 4)
                    .add(filters)
                    .add(sortOrder)
                    .add(startFrom)
                    .add(pageSize);
            boolean hasResults = call.execute();
            if (hasResults) {

                ResultSet rs = call.getResultSet();
                while (rs.next()) {
                    BotstoreItem item = new BotstoreItem(
                            order++,
                            this.getAiBotFromResultset(rs),
                            this.getDeveloperInfoFromBotstoreList(rs),
                            false);
                    list.add(item);
                }
                if (call.hasMoreResults()) {
                    rs = call.getResultSet();
                    if (rs.next()) {
                        totalResults = rs.getInt("total");
                    }
                }
            }

            return new ApiBotstoreItemList(list, startFrom, list.size(), totalResults);
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public BotstoreItem getBotstoreItem(final int botId) throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotstoreItem", 1).add(botId);
            ResultSet rs = call.executeQuery();
            if (rs.next()) {
                return new BotstoreItem(
                        0,
                        this.getAiBotFromResultset(rs),
                        this.getDeveloperInfoFromBotstoreList(rs),
                        false);
            }
            return null;
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public ApiBotstoreCategoryItemList getBotstoreItemsPerCategory(final int maxNumberOfItemsPerCategory)
            throws DatabaseException {
        try (DatabaseCall call = this.callProvider.get()) {
            call.initialise("getBotstoreListPerCategory", 1)
                    .add(maxNumberOfItemsPerCategory);
            ResultSet rs = call.executeQuery();
            String prevCategory = "";
            List<BotstoreItem> bots = new ArrayList<>();
            HashMap<String, List<BotstoreItem>> map = new HashMap<>();
            int order = 0;
            while (rs.next()) {
                final String category = rs.getString("category");
                if (!category.equals(prevCategory)) {
                    if (!bots.isEmpty()) {
                        map.put(prevCategory, bots);
                        bots = new ArrayList<>();
                    }
                    prevCategory = category;
                    order = 0;
                }
                BotstoreItem item = new BotstoreItem(
                        order++,
                        this.getAiBotFromResultset(rs),
                        this.getDeveloperInfoFromBotstoreList(rs),
                        false);
                bots.add(item);
            }
            if (!bots.isEmpty()) {
                map.put(prevCategory, bots);
            }
            return new ApiBotstoreCategoryItemList(map);
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private DeveloperInfo getDeveloperInfoFromBotstoreList(final ResultSet rs) throws SQLException {
        final String devIdString = rs.getString("dev_id");
        final UUID devId = UUID.fromString(devIdString);
        return new DeveloperInfo(
                devId,
                rs.getString("dev_name"),
                rs.getString("dev_company"),
                rs.getString("dev_email"),
                null,
                null,
                null,
                rs.getString("dev_country"),
                rs.getString("dev_website")
        );
    }
}
