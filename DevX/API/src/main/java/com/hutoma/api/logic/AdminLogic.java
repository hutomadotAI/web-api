package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAdmin;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AdminLogic {

    private static final String LOGFROM = "adminlogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final MessageQueue messageQueue;
    private final Logger logger;

    @Inject
    public AdminLogic(Config config, JsonSerializer jsonSerializer, Database database, MessageQueue messageQueue,
                      Logger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.messageQueue = messageQueue;
        this.logger = logger;
    }

    public ApiResult createDev(
            SecurityContext securityContext,
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
            this.logger.logInfo(LOGFROM, "request to create dev " + devId);

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
                this.logger.logError(LOGFROM, "db failed to create dev");
                return ApiError.getInternalServerError();
            }

            return new ApiAdmin(devToken, devId.toString()).setSuccessStatus("created successfully");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "failed to create dev: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteDev(
            SecurityContext securityContext,
            String devid) {

        try {
            this.logger.logInfo(LOGFROM, "request to delete dev " + devid);

            //TODO: distinguish between error condition and "failed to delete", perhaps because the dev was not found?
            if (!this.database.deleteDev(devid)) {
                this.logger.logInfo(LOGFROM, "db failed to delete dev");
                return ApiError.getBadRequest("not found or unable to delete");
            }
            this.messageQueue.pushMessageDeleteDev(devid);
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "failed to create dev: " + e.toString());
            return ApiError.getInternalServerError();
        }
        return new ApiResult().setSuccessStatus("deleted successfully");
    }

    public ApiResult getDevToken(SecurityContext securityContext, String devid) {
        try {
            this.logger.logInfo(LOGFROM, "request to get dev token " + devid);
            String devtoken = this.database.getDevToken(devid);

            if (devtoken.isEmpty()) {
                this.logger.logError(LOGFROM, "could not get dev token");
                return ApiError.getInternalServerError();
            }
            return new ApiAdmin(devtoken, devid).setSuccessStatus("token found");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "failed to get dev token: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

}
