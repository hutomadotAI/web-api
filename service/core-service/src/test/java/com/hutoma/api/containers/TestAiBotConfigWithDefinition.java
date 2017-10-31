package com.hutoma.api.containers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

public class TestAiBotConfigWithDefinition {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AllowEmptyConfig() throws AiBotConfigException {
        AiBotConfigWithDefinition config = new AiBotConfigWithDefinition(null, null);
        config.checkIsValid();
    }

    @Test
    public void test_descriptionWithNoConfig() throws AiBotConfigException {
        AiBotConfigDefinition definition = generateDefinition(1);
        AiBotConfigWithDefinition config = new AiBotConfigWithDefinition(null, definition);

        thrown.expect(AiBotConfigException.class);
        config.checkIsValid();
    }

    @Test
    public void test_noDescriptionWithConfig() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, null);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_matchingConfigOk() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(2);
        AiBotConfigDefinition definition = generateDefinition(2);
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        configDefs.checkIsValid();
    }

    @Test
    public void test_tooManyKeysFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(2);
        AiBotConfigDefinition definition = generateDefinition(1);
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_notEnoughManyKeysFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition = generateDefinition(2);
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_nullNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle(null);
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }


    @Test
    public void test_emptyNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle("");
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle("  ");
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceDescFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle("name", "  " ,"link");
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceLinkFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle("name", "desc", "   ");
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_duplicateNamesFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition definition =
                this.generateDefinitionSingle("duplicate_name");
        definition.getApiKeys().add(new AiBotConfigDefinition.ApiKeyDescription("duplicate_name", "blah", "link"));
        AiBotConfigWithDefinition configDefs = new AiBotConfigWithDefinition(config, definition);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    private AiBotConfigDefinition generateDefinition(int numberOfDesc) {
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = new ArrayList<>();
        for (int ii=0; ii<numberOfDesc; ii++) {
            descriptions.add(new AiBotConfigDefinition.ApiKeyDescription("name" + Integer.toString(ii),
                    "desc",
                    "link"));
        }
        AiBotConfigDefinition definition = new AiBotConfigDefinition(descriptions);
        return definition;
    }

    private AiBotConfigDefinition generateDefinitionSingle(String name, String description, String link) {
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                new ArrayList<>(Arrays.asList(new AiBotConfigDefinition.ApiKeyDescription(name, description, link)));
        AiBotConfigDefinition definition = new AiBotConfigDefinition(descriptions);
        return definition;
    }

    private AiBotConfigDefinition generateDefinitionSingle(String name) {
        return this.generateDefinitionSingle("name", "desc", "http://link.example.com");
    }

    private AiBotConfig generateConfig(int numberOfKeyValuePairs) {
        Map<String, String> keyValues = new HashMap<>();
        for (int ii=0; ii<numberOfKeyValuePairs; ii++) {
            keyValues.put("name" + Integer.toString(ii), "value");
        }

        AiBotConfig config = new AiBotConfig(keyValues);
        return config;
    }
}