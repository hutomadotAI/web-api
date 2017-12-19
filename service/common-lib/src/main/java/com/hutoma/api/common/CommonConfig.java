package com.hutoma.api.common;

import com.hutoma.api.logging.ILogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

public abstract class CommonConfig {

    protected final ILogger logger;
    private final HashSet<String> propertyLoaded;

    @Inject
    public CommonConfig(ILogger logger) {
        this.logger = logger;
        this.propertyLoaded = new HashSet<>();
    }

    protected abstract String getLoggingLogfrom();

    protected abstract String getEnvPrefix();

    public List<UUID> getAimlBotAiids() {
        List<String> stringList = getCSList("ai_aiml_bot_aiids");
        if (stringList == null) {
            stringList = Arrays.asList("e1bb8226-e8ce-467a-8305-bc2fcb89dd7f");
        }
        return stringList.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public boolean getDatabaseConnectionPoolLeakTracer() {
        return Boolean.parseBoolean(getConfigFromProperties("dbconnectionpool_trace_leaks", "false"));
    }

    protected List<String> getCSList(final String propertyName) {
        String instances = getConfigFromProperties(propertyName, null);
        if (instances == null) {
            return null;
        }
        if (!instances.isEmpty()) {
            return Arrays.asList(instances.split(","));
        }
        return new ArrayList<>();
    }

    private String getConfigFromEnvironment(String propertyName) {
        return System.getenv(getEnvPrefix() + propertyName.toUpperCase());
    }

    protected String getConfigFromProperties(String propertyName, String defaultValue) {
        String configFromEnv = getConfigFromEnvironment(propertyName);
        if (configFromEnv != null && !configFromEnv.isEmpty()) {
            return configFromEnv;
        }

        if (this.propertyLoaded.add(propertyName)) {
            if (defaultValue == null || defaultValue.isEmpty()) {
                this.logger.logWarning(getLoggingLogfrom(), String.format("No value found for property \"%s\"!",
                        propertyName));
            } else {
                this.logger.logWarning(getLoggingLogfrom(), String.format(
                        "No value found for property \"%s\". Using default value \"%s\"",
                        propertyName, defaultValue));
            }
            this.propertyLoaded.add(propertyName);

        }
        return defaultValue;
    }
}
