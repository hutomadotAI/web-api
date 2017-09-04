package com.hutoma.api.containers;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Class describing the Bot configuration
 */
public class AiBotConfigDefinition {
    private AiBotConfig config;
    private List<ApiKeyDescription> descriptions;

    public AiBotConfigDefinition(AiBotConfig config, List<ApiKeyDescription> descriptions) {
        this.config = config;
        this.descriptions = descriptions;
    }

    public AiBotConfig getConfig() {
        return config;
    }

    public List<ApiKeyDescription> getDescriptions() {
        return descriptions;
    }

    public static class ApiKeyDescription {
        private String name;
        private String description;
        private String link;

        public ApiKeyDescription(String name, String description, String link) {
            this.name = name;
            this.description = description;
            this.link = link;
        }
    }

    public boolean isEmpty() {
        if ((config == null || config.isEmpty()) && (descriptions == null || descriptions.isEmpty())) {
            return true;
        }
        return false;
    }

    public void checkIsValid() throws AiBotConfigException {
        if (isEmpty()) {
            // empty config is valid
            return;
        }

        if (this.descriptions == null || this.descriptions.isEmpty()) {
            throw new AiBotConfigException("Must provide a matching description for any configuration");
        }
        if (this.config == null || this.config.isEmpty()) {
            throw new AiBotConfigException("Must provide a matching configuration for any descriptions");
        }

        // validate the API descriptions
        Set<String> listOfKeys = new HashSet<>();
        for (ApiKeyDescription item: this.descriptions) {
            if (StringUtils.isBlank(item.name)) {
                throw new AiBotConfigException("API Key name cannot be blank");
            }
            String name = item.name;
            if (listOfKeys.contains(name)) {
                throw new AiBotConfigException(String.format("API Key '%s' already exists", name));
            }
            listOfKeys.add(name);
            
            if (StringUtils.isBlank(item.description)) {
                throw new AiBotConfigException(String.format("API Key '%s' must have a description", name));
            }
            if (StringUtils.isBlank(item.link)) {
                throw new AiBotConfigException(String.format("API Key '%s' must have a link", name));
            }
        }
        
        // validate the configs
        for (Map.Entry<String, String> keyValue : this.config.getApiKeys().entrySet()) {
            String key = keyValue.getKey();
            if (StringUtils.isBlank(key)) {
                throw new AiBotConfigException("Config Key name cannot be blank");
            }

            if (!listOfKeys.remove(key)) {
                throw new AiBotConfigException(String.format("Config is trying to set invalid key '%s'", key));
            }
        }

        if (!listOfKeys.isEmpty()) {
            throw new AiBotConfigException("Config is missing some keys that are required.");
        }
    }
}

