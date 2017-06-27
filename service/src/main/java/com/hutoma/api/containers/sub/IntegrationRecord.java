package com.hutoma.api.containers.sub;

import java.util.UUID;

public class IntegrationRecord {

    private String integrationResource;
    private String integrationUserid;
    private String data;
    private String status;
    private boolean active;

    private UUID aiid;
    private UUID devid;

    public IntegrationRecord(final String integrationResource, final String integrationUserid,
                             final String data, final String status, final boolean active) {
        this.integrationResource = integrationResource;
        this.integrationUserid = integrationUserid;
        this.data = data;
        this.status = status;
        this.active = active;
    }

    public IntegrationRecord(final UUID aiid, final UUID devid,
                             final String integrationUserid, final String data,
                             final String status, final boolean active) {
        this.integrationUserid = integrationUserid;
        this.data = data;
        this.status = status;
        this.aiid = aiid;
        this.devid = devid;
        this.active = active;
    }

    public String getData() {
        return this.data;
    }

    public boolean isActive() {
        return this.active;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getIntegrationResource() {
        return this.integrationResource;
    }

    public void setIntegrationResource(final String integrationResource) {
        this.integrationResource = integrationResource;
    }

    public String getIntegrationUserid() {
        return this.integrationUserid;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public UUID getDevid() {
        return this.devid;
    }
}
