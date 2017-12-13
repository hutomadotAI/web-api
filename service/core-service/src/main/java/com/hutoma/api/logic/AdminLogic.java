package com.hutoma.api.logic;

import com.hutoma.api.access.InvalidRoleException;
import com.hutoma.api.access.Role;
import com.hutoma.api.common.AuthHelper;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;
import com.hutoma.api.containers.ApiTokenRegenResult;
import com.hutoma.api.containers.ApiUserInfo;
import com.hutoma.api.containers.sub.UserInfo;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import io.jsonwebtoken.Claims;

import org.apache.logging.log4j.util.Strings;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AdminLogic {

    private static final String ADMIN_DEVID_LOG = "admin";
    private static final String LOGFROM = "adminlogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final DatabaseUser database;
    private final ILogger logger;
    private final AIServices aiServices;
    private final AIBotStoreLogic botStoreLogic;

    private static final List<Role> SUPPORTED_ROLES = Arrays.asList(Role.ROLE_FREE);

    @Inject
    public AdminLogic(final Config config, final JsonSerializer jsonSerializer, final DatabaseUser database,
                      final ILogger logger, final AIServices aiServices, final AIBotStoreLogic botStoreLogic) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
        this.aiServices = aiServices;
        this.botStoreLogic = botStoreLogic;
    }

    public ApiResult createDev(
            final String securityRole,
            final String username,
            final String email,
            final String password,
            final String passwordSalt,
            final String firstName,
            final String lastName,
            final int planId
    ) {
        try {
            // Limit the creation of the user to ROLE_FREE only, at least for now
            if (SUPPORTED_ROLES.stream().noneMatch(x -> x.name().equals(securityRole))) {
                this.logger.logUserErrorEvent(LOGFROM, "CreateDev - unsupported role", ADMIN_DEVID_LOG,
                        LogMap.map("Role", securityRole));
                return ApiError.getBadRequest(String.format("Role %s is not supported", securityRole));
            }
            // Now check the planId (artificially) matches the role
            Role role = Role.fromString(securityRole);
            if (planId != role.getPlan()) {
                this.logger.logUserErrorEvent(LOGFROM, "CreateDev - unsupported plan", ADMIN_DEVID_LOG,
                        LogMap.map("Plan", planId));
                return ApiError.getBadRequest(String.format("Plan %d is not supported", planId));
            }

            UUID devId = UUID.randomUUID();

            String devToken = AuthHelper.generateDevToken(devId, securityRole, this.config.getEncodingKey());

            if (!this.database.createDev(username, email, password, passwordSalt, firstName, lastName,
                    devToken, planId, devId.toString())) {
                this.logger.logUserErrorEvent(LOGFROM, "CreateDev - failed to create", ADMIN_DEVID_LOG,
                        LogMap.map("Email", email));
                return ApiError.getInternalServerError();
            }

            this.logger.logUserTraceEvent(LOGFROM, "CreateDev", ADMIN_DEVID_LOG, LogMap.map("DevId", devId));

            // auto purchase the default bots
            autoPurchaseBots(devId);

            return new ApiAdmin(devToken, devId).setSuccessStatus("created successfully");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "CreateDev", ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }
    }

    private void autoPurchaseBots(final UUID devId) {
        LogMap logMap = LogMap.map("DevId", devId);
        List<String> autoPurchaseList = config.getAutoPurchaseBotIds();
        if (!autoPurchaseList.isEmpty()) {
            boolean hasErrors = false;
            for (String botId : autoPurchaseList) {
                ApiResult result = botStoreLogic.purchaseBot(devId, Integer.parseInt(botId));
                if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                    hasErrors = true;
                    this.logger.logUserWarnEvent(LOGFROM, String.format("Failed to auto-purchase bot %s", botId),
                            devId.toString(), logMap.put("ErrorCode", result.getStatus().getCode()));
                }
            }
            if (!hasErrors) {
                this.logger.logUserTraceEvent(LOGFROM, String.format("Auto-purchased %d bots", autoPurchaseList.size()),
                        devId.toString(), logMap.put("BotList", Strings.join(autoPurchaseList, ',')));
            }
        }
    }

    public ApiResult deleteDev(final UUID devId) {
        if (devId == null) {
            return ApiError.getBadRequest("null devId sent");
        }

        LogMap logMap = LogMap.map("DevId", devId);
        try {
            //TODO: distinguish between error condition and "failed to delete", perhaps because the dev was not found?
            if (!this.database.deleteDev(devId)) {
                this.logger.logUserWarnEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, logMap);
                return ApiError.getBadRequest("not found or unable to delete");
            }
            this.aiServices.deleteDev(devId);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, logMap);
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleveDev", ADMIN_DEVID_LOG, e, logMap);
            return ApiError.getInternalServerError();
        }

        this.logger.logUserTraceEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, logMap);
        return new ApiResult().setSuccessStatus("deleted successfully");
    }

    public ApiResult getDevToken(final UUID devid) {
        LogMap logMap = LogMap.map("DevId", devid);
        try {
            String devtoken = this.database.getDevToken(devid);
            if (devtoken == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetDevToken - not found", ADMIN_DEVID_LOG, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetDevToken", ADMIN_DEVID_LOG, logMap);
            return new ApiAdmin(devtoken, devid).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetDevToken", ADMIN_DEVID_LOG, e, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult doesUserExist(final String username, final boolean checkEmail) {
        try {
            if (!this.database.userExists(username, checkEmail)) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetUser", ADMIN_DEVID_LOG, ex,
                    LogMap.map("UserName", username));
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getUser(final String username) {
        LogMap logMap = LogMap.map("UserName", username);
        try {
            UserInfo userInfo = this.database.getUser(username);
            if (userInfo == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetUser - not found", ADMIN_DEVID_LOG, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetUser", ADMIN_DEVID_LOG, logMap);
            return new ApiUserInfo(userInfo).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetUser", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateLoginAttempts(final String devId, final String loginAttempts) {
        LogMap logMap = LogMap.map("DevId", devId);
        try {
            if (this.database.updateUserLoginAttempts(devId, loginAttempts)) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateLoginAttempts", devId,
                        logMap.put("LoginAttempts", loginAttempts));
                return new ApiResult().setSuccessStatus();
            }
            this.logger.logUserTraceEvent(LOGFROM, "UpdateLoginAttempts - not found", devId, logMap);
            return ApiError.getBadRequest();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateLoginAttempts", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateUserPassword(final int userId, final String password, final String passwordSalt) {
        LogMap logMap = LogMap.map("UserId", userId);
        try {
            if (this.database.updateUserPassword(userId, password, passwordSalt)) {
                return new ApiResult().setSuccessStatus();
            }
            return ApiError.getBadRequest();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateUserPassword", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult isPasswordResetTokenValid(final String token) {
        LogMap logMap = LogMap.map("Token", token);
        try {
            if (this.database.isPasswordResetTokenValid(token)) {
                this.logger.logUserTraceEvent(LOGFROM, "IsPasswordResetTokenValid", ADMIN_DEVID_LOG, logMap);
                return new ApiResult().setSuccessStatus();
            }
            this.logger.logUserTraceEvent(LOGFROM, "IsPasswordResetTokenValid - not found", ADMIN_DEVID_LOG,
                    logMap);
            return ApiError.getNotFound();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "IsPasswordResetTokenValid", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getUserIdForResetToken(final String token) {
        LogMap logMap = LogMap.map("Token", token);
        try {
            int userId = this.database.getUserIdForResetToken(token);
            if (userId >= 0) {
                this.logger.logUserTraceEvent(LOGFROM, "GetUserIdForResetToken", ADMIN_DEVID_LOG, logMap);
                return new ApiString(Integer.toString(userId)).setSuccessStatus();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetUserIdForResetToken - not found", ADMIN_DEVID_LOG,
                    logMap);
            return ApiError.getNotFound();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetUserIdForResetToken", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deletePasswordResetToken(final String token) {
        LogMap logMap = LogMap.map("Token", token);
        try {
            if (this.database.deletePasswordResetToken(token)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeletePasswordResetToken", ADMIN_DEVID_LOG, logMap);
                return new ApiResult().setSuccessStatus();
            }
            this.logger.logUserTraceEvent(LOGFROM, "DeletePasswordResetToken - not found", ADMIN_DEVID_LOG,
                    logMap);
            return ApiError.getNotFound();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeletePasswordResetToken", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult insertPasswordResetToken(final int userId, final String token) {
        LogMap logMap = LogMap.map("Token", token).put("UserId", userId);
        try {
            if (this.database.insertPasswordResetToken(userId, token)) {
                this.logger.logUserTraceEvent(LOGFROM, "InsertPasswordResetToken", ADMIN_DEVID_LOG, logMap);
                return new ApiResult().setSuccessStatus();
            }
            this.logger.logUserTraceEvent(LOGFROM, "InsertPasswordResetToken - not found", ADMIN_DEVID_LOG,
                    logMap);
            return ApiError.getNotFound();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "InsertPasswordResetToken", ADMIN_DEVID_LOG, ex, logMap);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult regenerateTokens(final String devId, final boolean dryrun) {
        try {
            final String encodingKey = this.config.getEncodingKey();
            if (devId != null) {
                try {
                    UUID uuidDevId = UUID.fromString(devId);
                    UserInfo user = this.database.getUserFromDevId(uuidDevId);
                    boolean updated = regenerateDevTokenIfNeeded(user, encodingKey, dryrun);
                    ApiResult result = updated
                        ? new ApiResult().setCreatedStatus()
                        : new ApiResult().setSuccessStatus();
                    this.logger.logUserInfoEvent(LOGFROM, "RegenerateTokens-single", ADMIN_DEVID_LOG,
                            LogMap.map("DevId", devId)
                                    .put("Updated", updated)
                                    .put("Dry run", dryrun));
                    return result;
                } catch (IllegalArgumentException ex) {
                    return ApiError.getBadRequest(String.format("DevId %s is not valid", devId));
                } catch (Exception ex) {
                    this.logger.logUserExceptionEvent(LOGFROM, "RegenerateTokens-single", ADMIN_DEVID_LOG, ex);
                    return ApiError.getInternalServerError();
                }
            } else {
                List<UserInfo> users = this.database.getAllUsers();
                ApiTokenRegenResult result = new ApiTokenRegenResult();
                for (UserInfo user: users) {
                    try {
                        boolean updated = regenerateDevTokenIfNeeded(user, encodingKey, dryrun);
                        if (updated) {
                            result.addUpdated(UUID.fromString(user.getDevId()));
                        } else {
                            result.addSkipped(UUID.fromString(user.getDevId()));
                        }
                    } catch (Exception ex) {
                        result.addError(UUID.fromString(user.getDevId()), ex.getMessage());
                    }
                }
                this.logger.logUserInfoEvent(LOGFROM, "RegenerateTokens-multiple", ADMIN_DEVID_LOG,
                        LogMap.map("NumUpdated", result.getUpdated().size())
                            .put("NumSkipped", result.getSkipped().size())
                            .put("NumErrors", result.getErrors().size())
                            .put("Dry run", dryrun));
                return result.setSuccessStatus();
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "RegenerateTokens-multiple", ADMIN_DEVID_LOG, ex);
            return ApiError.getInternalServerError();
        }
    }

    private boolean regenerateDevTokenIfNeeded(final UserInfo user, final String encodingKey, final boolean dryrun)
            throws InvalidRoleException, DatabaseException {
        // Skip any users that don't have a dev token as these are special user types (cannot perform actions)
        if (user.getDevToken() == null || user.getDevToken().isEmpty()) {
            return false;
        }

        boolean needsRegen = false;
        // Determine if we can open the token stored for the user
        final String token = user.getDevToken();
        try {
            final Claims claims = AuthHelper.getClaimsFromToken(token, encodingKey);
            final String expectedDevId = claims.getSubject();
            if (!user.getDevId().equals(expectedDevId)) {
                needsRegen = true;
            }
            AuthHelper.getRoleFromClaims(claims);
        } catch (Exception ex) {
            needsRegen = true;
        }

        if (needsRegen) {
            UUID uuid = UUID.fromString(user.getDevId());
            Role role = Role.fromPlan(user.getPlanId());
            final String newToken = AuthHelper.generateDevToken(uuid, role.name(), encodingKey);
            if (!dryrun) {
                if (!this.database.updateUserDevToken(uuid, newToken)) {
                    throw new DatabaseException("user not added");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
