package com.hutoma.api.containers;

import com.google.gson.annotations.SerializedName;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;

public class ServiceIdentity {

    public static final String DEFAULT_VERSION = "default";

    @SerializedName("server_type")
    private BackendServerType serverType;

    @SerializedName("language")
    private SupportedLanguage language;

    @SerializedName("version")
    private String version;

    public ServiceIdentity(final BackendServerType serverType,
                           final SupportedLanguage language,
                           final String version) {
        this.serverType = serverType;
        this.language = language;
        this.version = version;
    }


    public static ServiceIdentity getAimlIdent() {
        return new ServiceIdentity(BackendServerType.AIML, SupportedLanguage.EN, null);
    }

    public BackendServerType getServerType() {
        return this.serverType;
    }

    public SupportedLanguage getLanguage() {
        return this.language;
    }

    public String getVersion() {
        return this.version;
    }

    public void setLanguage(final SupportedLanguage language) {
        this.language = language;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]-%s",
                this.getServerType().value(),
                this.getLanguage().toString(),
                this.getVersion() == null ? DEFAULT_VERSION : this.getVersion());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
