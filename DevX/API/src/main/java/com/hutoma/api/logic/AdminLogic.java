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

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AdminLogic {

    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    MessageQueue messageQueue;
    Logger logger;

    private final String LOGFROM = "adminlogic";

    @Inject
    public AdminLogic(Config config, JsonSerializer jsonSerializer, Database database, MessageQueue messageQueue, Logger logger) {
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
            String first_name,
            String last_name,
            int planId
    ) {

        try {

            UUID dev_id = UUID.randomUUID();
            String encoding_key = config.getEncodingKey();
            logger.logInfo(LOGFROM, "request to create dev " + dev_id);

            String devToken = Jwts.builder()
                    .claim("ROLE", securityRole)
                    .setSubject(dev_id.toString())
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();

            String clientToken = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .setSubject(dev_id.toString())
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();

            if (!database.createDev(username, email, password, passwordSalt, first_name, last_name, devToken.toString(), planId,dev_id.toString(),clientToken.toString())) {
                logger.logError(LOGFROM, "db failed to create dev");
                return ApiError.getInternalServerError();
            }

            return new ApiAdmin(devToken, dev_id.toString()).setSuccessStatus("created successfully");
        }
        catch (Exception e) {
            logger.logError(LOGFROM, "failed to create dev: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteDev(
            SecurityContext securityContext,
            String devid) {

        try {
            logger.logInfo(LOGFROM, "request to delete dev " + devid);

            //TODO: distinguish between error condition and "failed to delete", perhaps because the dev was not found?
            if (!database.deleteDev(devid)) {
                logger.logInfo(LOGFROM, "db failed to delete dev");
                return ApiError.getBadRequest("not found or unable to delete");
            }
            messageQueue.pushMessageDeleteDev(devid);
        }
        catch (Exception e){
            logger.logError(LOGFROM, "failed to create dev: " + e.toString());
            return ApiError.getInternalServerError();
        }
        return new ApiResult().setSuccessStatus("deleted successfully");
    }
}
