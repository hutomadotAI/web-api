package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseUI;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.AiSkillSummary;
import com.hutoma.api.containers.ui.ApiAiDetails;
import com.hutoma.api.containers.ui.ApiBotstoreCategoryItemList;
import com.hutoma.api.containers.ui.ApiBotstoreItem;
import com.hutoma.api.containers.ui.ApiBotstoreItemList;
import com.hutoma.api.containers.ui.BotstoreItem;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Logic file to handle UI-specific requests.
 */
public class UILogic {
    private static final String LOGFROM = "uilogic";
    private final DatabaseUI databaseUi;
    private final DatabaseAI databaseAi;
    private final DatabaseMarketplace databaseMarketplace;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final ILogger logger;
    private final JsonSerializer serializer;

    @Inject
    public UILogic(final DatabaseUI databaseUi, final DatabaseMarketplace databaseMarketplace,
            final DatabaseAI databaseAi, final DatabaseEntitiesIntents databaseEntitiesIntents, final ILogger logger,
            final JsonSerializer serializer) {
        this.databaseUi = databaseUi;
        this.databaseAi = databaseAi;
        this.databaseMarketplace = databaseMarketplace;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.logger = logger;
        this.serializer = serializer;
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
    public ApiResult getBotstoreList(final UUID devId, final int startFrom, final int pageSize,
            final List<String> filterList, final String orderField, final String orderDirection) {
        try {
            ApiBotstoreItemList list = this.databaseUi.getBotstoreList(startFrom, pageSize, filterList, orderField,
                    orderDirection);
            if (devId != null) {
                List<AiBot> ownedBots = this.databaseMarketplace.getPurchasedBots(devId);
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
     * Gets a list od botstore items divided by category.
     * In all effects this is a map keyed by the category name, in wich each has a list of bots for that category.
     * @param devId            the developer id (or null if public access)
     * @param maxNumberOfItems maximum number of items per category
     * @return the map of botstore items
     */
    public ApiResult getBotstoreListPerCategory(final UUID devId, final int maxNumberOfItems) {
        try {
            ApiBotstoreCategoryItemList map = this.databaseUi.getBotstoreItemsPerCategory(maxNumberOfItems);
            if (devId != null) {
                List<AiBot> ownedBots = this.databaseMarketplace.getPurchasedBots(devId);
                HashSet<Integer> ownedSet = new HashSet<>();
                ownedBots.forEach(x -> ownedSet.add(x.getBotId()));
                for (Map.Entry<String, List<BotstoreItem>> entry : map.getCategoriesMap().entrySet()) {
                    for (BotstoreItem b : entry.getValue()) {
                        if (ownedSet.contains(b.getMetadata().getBotId())) {
                            b.setOwned(true);
                        }
                    }
                }
            }
            return map.setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "getBotstoreListPerCategory", null, ex);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Gets an item from the botstore.
     * @param devId the developer id
     * @param botId the bot id
     * @return the item
     */
    public ApiResult getBotstoreBot(final UUID devId, final int botId) {
        try {
            BotstoreItem item = this.databaseUi.getBotstoreItem(botId, this.serializer);
            if (item == null) {
                return ApiError.getNotFound();
            }

            if (devId != null) {
                List<AiBot> ownedBots = this.databaseMarketplace.getPurchasedBots(devId);
                if (ownedBots.stream().anyMatch(x -> x.getBotId() == botId)) {
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

    /**
     * Gets the details for an AI
     * @param devId the developer id
     * @param aiid the AI id
     * @return the AI details
     */
    public ApiResult getAiDetails(final UUID devId, final UUID aiid) {
        if (devId == null || aiid == null) {
            this.logger.logError(LOGFROM, "GetAiDetails - no AIID or DevId");
            return ApiError.getBadRequest();
        }
        LogMap logMap = LogMap.map("AIID", aiid).put("DevId", devId);
        try {
            ApiAi ai = this.databaseAi.getAI(devId, aiid, serializer);
            if (ai == null) {
                this.logger.logUserErrorEvent(LOGFROM, "GetAiDetails - not found", devId.toString(), logMap);
                return ApiError.getNotFound();
            }
            String trainingFile = this.databaseAi.getAiTrainingFile(aiid);
            List<String> intentNames = this.databaseEntitiesIntents.getIntents(devId, aiid);
            List<AiBot> linkedBots = this.databaseAi.getBotsLinkedToAi(devId, aiid);
            List<AiSkillSummary> skills = new ArrayList<>();
            for (AiBot bot : linkedBots) {
                skills.add(new AiSkillSummary(bot));
            }
            this.logger.logUserInfoEvent(LOGFROM, "GetAiDetails", devId.toString(), logMap);
            return new ApiAiDetails(trainingFile, intentNames, skills).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "getAiDetails", null, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }
}
