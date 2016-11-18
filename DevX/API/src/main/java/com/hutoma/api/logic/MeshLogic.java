package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiMeshList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.MeshVariable;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 05/10/2016.
 */
public class MeshLogic {

    private static final String LOGFROM = "meshlogic";
    private final Config config;
    private final ILogger logger;
    private final DatabaseEntitiesIntents database;

    @Inject
    public MeshLogic(final Config config, final ILogger logger, final DatabaseEntitiesIntents database) {
        this.config = config;
        this.logger = logger;
        this.database = database;
    }

    public ApiResult getMesh(final SecurityContext securityContext, final String devid, final UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "request to list mesh from devid:" + devid + ", aiid:" + aiid);
            final List<MeshVariable> mv = this.database.getMesh(devid, aiid.toString());
            if (mv.isEmpty()) {
                return ApiError.getNotFound();
            }
            return new ApiMeshList(mv).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error getting mesh: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult addMesh(final SecurityContext securityContext, final String devid, final UUID aiid, final UUID aiid_mesh) {
        try {
            this.logger.logDebug(LOGFROM, "request to add mesh:" + aiid_mesh.toString() + "from devid:" + devid + ", aiid:" + aiid);
            if (!this.database.addMesh(devid, aiid.toString(), aiid_mesh.toString())) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error adding mesh: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteSingleMesh(final SecurityContext securityContext, final String devid, final UUID aiid, final UUID aiid_mesh) {
        try {
            this.logger.logDebug(LOGFROM, "request to delete mesh:" + aiid_mesh.toString() + " from devid:" + devid + ", aiid:" + aiid);
            if (!this.database.deleteSingleMesh(devid, aiid.toString(), aiid_mesh.toString())) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error deleting single mesh: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }


    public ApiResult deleteAllMesh(final SecurityContext securityContext, final String devid, final UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "request to delete all mesh from devid:" + devid + ", aiid:" + aiid);
            if (!this.database.deleteAllMesh(devid, aiid.toString())) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logError(LOGFROM, "error delering mesh: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }


}
