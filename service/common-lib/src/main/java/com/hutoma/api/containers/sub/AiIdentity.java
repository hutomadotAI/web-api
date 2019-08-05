package com.hutoma.api.containers.sub;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ServiceIdentity;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

public class AiIdentity {
    private final UUID devId;
    private final UUID aiid;
    private SupportedLanguage language;
    private String serverVersion;

    public AiIdentity(final UUID devId, final UUID aiid) {
        this(devId, aiid, SupportedLanguage.EN);
    }

    public AiIdentity(final UUID devId, final UUID aiid, final SupportedLanguage language) {
        this(devId, aiid, language, ServiceIdentity.DEFAULT_VERSION);
    }

    public AiIdentity(final UUID devId, final UUID aiid, final SupportedLanguage language, final String serverVersion) {
        this.devId = devId;
        this.aiid = aiid;
        this.language = language;
        this.serverVersion = serverVersion;
    }

    public UUID getDevId() {
        return this.devId;
    }

    public UUID getAiid() {
        return this.aiid;
    }

    public SupportedLanguage getLanguage() {
        return this.language;
    }

    public void setServerVersion(final String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerVersion() {
        if (Tools.isEmpty(this.serverVersion)) {
            return ServiceIdentity.DEFAULT_VERSION;
        }
        return this.serverVersion;
    }

}
