package com.hutoma.api.containers;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Class describing the Bot configuration, combined with definition
 */
public class AiBotConfigWithDefinition {
    private AiBotConfig config;
    private AiBotConfigDefinition definition;

    public AiBotConfigWithDefinition(AiBotConfig config, AiBotConfigDefinition definition) {
        this.config = config;
        this.definition = definition;
    }

    public AiBotConfig getConfig() {
        return config;
    }

    public AiBotConfigDefinition getDefinitions() {
        return definition;
    }

    public boolean isEmpty() {
        if ((config == null || config.isEmpty()) && (definition == null || definition.isEmpty())) {
            return true;
        }
        return false;
    }

    public void checkIsValid() throws AiBotConfigException {
        if (isEmpty()) {
            // empty config is valid
            return;
        }

        if (this.definition == null || this.definition.isEmpty()) {
            throw new AiBotConfigException("Must provide a matching description for any configuration");
        }
        if (this.config == null || this.config.isEmpty()) {
            throw new AiBotConfigException("Must provide a matching configuration for any descriptions");
        }

        // validate the API descriptions
        Set<String> listOfKeys = new HashSet<>();
        for (AiBotConfigDefinition.ApiKeyDescription item: this.definition.getApiKeys()) {
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

