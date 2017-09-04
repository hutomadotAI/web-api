package com.hutoma.api.containers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

public class TestAiBotConfigDefinition {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_AllowEmptyConfig() throws AiBotConfigException {
        AiBotConfigDefinition config = new AiBotConfigDefinition(null, null);
        config.checkIsValid();
    }

    @Test
    public void test_descriptionWithNoConfig() throws AiBotConfigException {
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = generateConfigDescription(1);
        AiBotConfigDefinition config = new AiBotConfigDefinition(null, descriptions);

        thrown.expect(AiBotConfigException.class);
        config.checkIsValid();
    }

    @Test
    public void test_noDescriptionWithConfig() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, null);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_matchingConfigOk() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(2);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = generateConfigDescription(2);
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        configDefs.checkIsValid();
    }

    @Test
    public void test_tooManyKeysFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(2);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = generateConfigDescription(1);
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_notEnoughManyKeysFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = generateConfigDescription(2);
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_nullNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle(null);
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }


    @Test
    public void test_emptyNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle("");
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceNameFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle("  ");
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceDescFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle("name", "  " ,"link");
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_whitespaceLinkFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle("name", "desc", "   ");
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    @Test
    public void test_duplicateNamesFail() throws AiBotConfigException {
        AiBotConfig config = this.generateConfig(1);
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                this.generateConfigDescriptionSingle("duplicate_name");
        descriptions.add(new AiBotConfigDefinition.ApiKeyDescription("duplicate_name", "blah", "link"));
        AiBotConfigDefinition configDefs = new AiBotConfigDefinition(config, descriptions);

        thrown.expect(AiBotConfigException.class);
        configDefs.checkIsValid();
    }

    private List<AiBotConfigDefinition.ApiKeyDescription> generateConfigDescription(int numberOfDesc) {
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions = new ArrayList<>();
        for (int ii=0; ii<numberOfDesc; ii++) {
            descriptions.add(new AiBotConfigDefinition.ApiKeyDescription("name" + Integer.toString(ii),
                    "desc",
                    "link"));
        }
        return descriptions;
    }

    private List<AiBotConfigDefinition.ApiKeyDescription> generateConfigDescriptionSingle(String name, String description, String link) {
        List<AiBotConfigDefinition.ApiKeyDescription> descriptions =
                new ArrayList<>(Arrays.asList(new AiBotConfigDefinition.ApiKeyDescription(name, description, link)));
        return descriptions;
    }

    private List<AiBotConfigDefinition.ApiKeyDescription> generateConfigDescriptionSingle(String name) {
        return this.generateConfigDescriptionSingle("name", "desc", "http://link.example.com");
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