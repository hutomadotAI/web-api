package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApiTokenRegenResult extends ApiResult {

    @SerializedName("errors")
    private final Map<UUID, String> errors;
    @SerializedName("skipped")
    private final List<UUID> skipped;
    @SerializedName("updated")
    private final List<UUID> updated;

    public ApiTokenRegenResult() {
        this.errors = new HashMap<>();
        this.skipped = new ArrayList<>();
        this.updated = new ArrayList<>();
    }

    public ApiTokenRegenResult(final Map<UUID, String> errors, final List<UUID> skipped, final List<UUID> updated) {
        this.errors = errors;
        this.skipped = skipped;
        this.updated = updated;
    }

    public void addError(final UUID devId, final String error) {
        this.errors.put(devId, error);
    }

    public void addSkipped(final UUID devId) {
        this.skipped.add(devId);
    }

    public void addUpdated(final UUID devId) {
        this.updated.add(devId);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<UUID> getSkipped() {
        return this.skipped;
    }

    public List<UUID> getUpdated() {
        return this.updated;
    }

    public Map<UUID, String> getErrors() {
        return this.errors;
    }
}
