package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.DatabaseUI;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.ui.ApiBotstoreItem;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;

import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;

/**
 * Logic file to handle UI-specific requests.
 */
public class UILogic {
    private static final String LOGFROM = "uilogic";
    private final DatabaseUI databaseUi;
    private final ILogger logger;

    @Inject
    public UILogic(final DatabaseUI databaseUi, final ILogger logger) {
        this.databaseUi = databaseUi;
        this.logger = logger;
    }

    /**
     * Gets a list of botstore items.
     * @param devId          the developer id (or null if public access)
     * @param startFrom      pagination start from record
     * @param pageSize       pagination page size
     * @param filterList     list of filters
     * @param orderField     field to sort by
     * @param orderDirection sort direction
     * @return the ApiResult containing the list of botstore items, or an error.
     */
    public ApiResult getBotstoreList(final String devId, final int startFrom, final int pageSize,
                                     final List<String> filterList,
                                     final String orderField, final String orderDirection) {
        try {
            ApiBotstoreItemList list = this.databaseUi.getBotstoreList(
                    startFrom, pageSize, filterList, orderField, orderDirection);
            if (devId != null) {
                List<AiBot> ownedBots = this.databaseUi.getPurchasedBots(devId);
                HashSet<Integer> ownedSet = new HashSet<>();
                ownedBots.forEach(x -> ownedSet.add(x.getBotId()));
                for (int i = 0; i < list.getItems().size(); i++) {
                    if (ownedSet.contains(list.getItems().get(i).getMetadata().getBotId())) {
                        list.getItems().get(i).setOwned(true);
                    }
                }
            }
            return list.setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "getBotstoreList", null, ex);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Gets an item from the botstore.
     * @param devId the developer id
     * @param botId the bot id
     * @return the item
     */
    public ApiResult getBotstoreBot(final String devId, final int botId) {
        try {
            BotstoreItem item = this.databaseUi.getBotstoreItem(botId);
            if (item == null) {
                return ApiError.getNotFound();
            }
            
            if (devId != null) {
                List<AiBot> ownedBots = this.databaseUi.getPurchasedBots(devId);
                if (ownedBots.stream().filter(x -> x.getBotId() == botId).findAny().isPresent()) {
                    item.setOwned(true);
                }
            }
            ApiBotstoreItem result = new ApiBotstoreItem(item);
            return result.setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "getBotstoreBot", null, ex);
            return ApiError.getInternalServerError();
        }
    }
}
