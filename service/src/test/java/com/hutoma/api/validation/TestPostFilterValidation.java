package com.hutoma.api.validation;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiBotStructure;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.IntentVariable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * Created by bretc on 11/10/2017.
 */
public class TestPostFilterValidation {

    private PostFilter postFilter;
    private ILogger fakeLogger;
    private JsonSerializer fakeSerializer;
    private Tools fakeTools;

    @Before
    public void setup() {
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeSerializer = mock(JsonSerializer.class);
        this.postFilter = new PostFilter(this.fakeLogger, this.fakeTools, this.fakeSerializer);
    }

    @Test
    public void validateV1BotStructure() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception, it's valid.
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateV1BotStructureInvalidAiName() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setName("<?php new name alert(); ?>#_+~\\");
        postFilter.validateBotStructure(botStructure);
    }

    @Test
    public void validateV1BotStructureEmptyTrainingFile() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setTrainingFile("");
        postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception, it's valid.
    }

    @Test
    public void validatV1BotStructureAllowsEN_US() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setLanguage("en_US");
        postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception it's valid.
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateV1BotStructureInvalidTimeZone() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setTimezone("tz");
        postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validatV1BotStructureInvalidLanguage() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setLanguage("en-EN");
        postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateBotStructureInvalidEntity() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        HashMap<String, ApiEntity> entities = botStructure.getEntities();
        List<String> entityValues = new ArrayList<>();
        entityValues.add("_+[[*invalid");
        entities.put("invalid", new ApiEntity("_=]InvalidName", UUID.randomUUID(), entityValues, false));
        botStructure.setEntities(entities);
        postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateBotStructureInvalidIntent() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        List<ApiIntent> intents = botStructure.getIntents();
        ApiIntent intent = new ApiIntent("intent_name", "topicIn", "topicOut");
        intent.addUserSays("valid question");
        intent.addResponse("valid response");
        intent.addVariable(new IntentVariable("*-=", UUID.randomUUID(), true, 3,
                "valid value", false, ";'["));
        intents.add(intent);
        botStructure.setIntents(intents);
        postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateV1BotStructureNameLength() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setName(String.join("", Collections.nCopies(640, "A")));
        postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = Validate.ParameterValidationException.class)
    public void validateV1BotStructureDescriptionLength() throws Validate.ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setDescription(String.join("", Collections.nCopies(640, "A")));
        postFilter.validateBotStructure(botStructure);
    }

    private BotStructure generateValidV1BotStructure() {
        final String label = "valid_label";
        HashMap<String, ApiEntity> entities = new HashMap<>();
        List<String> entityValues = new ArrayList<>();
        entityValues.add("valid value");
        entityValues.add("valid second value");
        ApiEntity entity = new ApiEntity("entity_name", UUID.randomUUID(), entityValues, false);
        entities.put(label, entity);
        List<ApiIntent> intents = new ArrayList<>();
        ApiIntent intent = new ApiIntent("intent_name", "topicIn", "topicOut");
        intent.addUserSays("valid question");
        intent.addResponse("valid response");
        intent.addVariable(new IntentVariable(entity.getEntityName(), entity.getDevOwner(), true, 3,
                "valid value", false, label));
        intents.add(intent);
        BotStructure structure = new BotStructure("Valid Name", "Valid Description", intents,
                "hello\nhi", entities, 1, true, 0, 0.5f,
                1, "en-US", "Europe/London");
        return structure;
    }
}
