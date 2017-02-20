package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AdminLogic {

    public static final String ADMIN_DEVID_LOG = "admin";
    private static final String LOGFROM = "adminlogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final ILogger logger;
    private final AIServices aiServices;

    @Inject
    public AdminLogic(Config config, JsonSerializer jsonSerializer, Database database,
                      ILogger logger, AIServices aiServices) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
        this.aiServices = aiServices;
    }

    public ApiResult createDev(
            String securityRole,
            String username,
            String email,
            String password,
            String passwordSalt,
            String firstName,
            String lastName,
            int planId
    ) {

        try {

            UUID devId = UUID.randomUUID();
            String encodingKey = this.config.getEncodingKey();

            String devToken = Jwts.builder()
                    .claim("ROLE", securityRole)
                    .setSubject(devId.toString())
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encodingKey)
                    .compact();

            String clientToken = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .setSubject(devId.toString())
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encodingKey)
                    .compact();

            if (!this.database.createDev(username, email, password, passwordSalt, firstName, lastName,
                    devToken, planId, devId.toString(), clientToken)) {
                this.logger.logUserErrorEvent(LOGFROM, "CreateDev - failed to create", ADMIN_DEVID_LOG, "Email", email);
                return ApiError.getInternalServerError();
            }
            this.logger.logUserTraceEvent(LOGFROM, "CreateDev", ADMIN_DEVID_LOG, "DevId", devId.toString());
            return new ApiAdmin(devToken, devId.toString()).setSuccessStatus("created successfully");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "CreateDev", ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteDev(final String devId) {

        try {
            //TODO: distinguish between error condition and "failed to delete", perhaps because the dev was not found?
            if (!this.database.deleteDev(devId)) {
                this.logger.logUserWarnEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, "DevId", devId);
                return ApiError.getBadRequest("not found or unable to delete");
            }
            this.aiServices.deleteDev(devId);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, "DevId", devId);
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleveDev", ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }

        this.logger.logUserTraceEvent(LOGFROM, "DeleteDev", ADMIN_DEVID_LOG, "DevId", devId);
        return new ApiResult().setSuccessStatus("deleted successfully");
    }

    public ApiResult getDevToken(final String devid) {
        try {
            String devtoken = this.database.getDevToken(devid);
            if (devtoken == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetDevToken - not found", ADMIN_DEVID_LOG, "DevId", devid);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetDevToken", ADMIN_DEVID_LOG, "DevId", devid);
            return new ApiAdmin(devtoken, devid).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetDevToken", ADMIN_DEVID_LOG, e);
            return ApiError.getInternalServerError();
        }
    }
}
