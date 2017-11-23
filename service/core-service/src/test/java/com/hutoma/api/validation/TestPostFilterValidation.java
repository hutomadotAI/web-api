package com.hutoma.api.validation;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.logging.ILogger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void validateV1BotStructure() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        this.postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception, it's valid.
    }

    @Test(expected = ParameterValidationException.class)
    public void validateV1BotStructureInvalidAiName() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setName("<?php new name alert(); ?>#_+~\\");
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test
    public void validateV1BotStructureEmptyTrainingFile() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setTrainingFile("");
        this.postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception, it's valid.
    }

    @Test
    public void validatV1BotStructureAllowsEN_US() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setLanguage("en_US");
        this.postFilter.validateBotStructure(botStructure);
        // If the test doesn't throw an exception it's valid.
    }

    @Test(expected = ParameterValidationException.class)
    public void validateV1BotStructureInvalidTimeZone() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setTimezone("tz");
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = ParameterValidationException.class)
    public void validatV1BotStructureInvalidLanguage() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setLanguage("en-EN");
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateBotStructureInvalidEntity() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        HashMap<String, ApiEntity> entities = botStructure.getEntities();
        List<String> entityValues = new ArrayList<>();
        entityValues.add("_+[[*invalid");
        entities.put("invalid", new ApiEntity("_=]InvalidName", UUID.randomUUID(), entityValues, false));
        botStructure.setEntities(entities);
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateBotStructureInvalidIntent() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        List<ApiIntent> intents = botStructure.getIntents();
        ApiIntent intent = new ApiIntent("intent_name", "topicIn", "topicOut");
        intent.addUserSays("valid question");
        intent.addResponse("valid response");
        intent.addVariable(new IntentVariable("*-=", UUID.randomUUID(), true, 3,
                "valid value", false, ";'["));
        intents.add(intent);
        botStructure.setIntents(intents);
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateV1BotStructureNameLength() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setName(TestDataHelper.stringOfLength(640));
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateV1BotStructureDescriptionLength() throws ParameterValidationException {
        BotStructure botStructure = generateValidV1BotStructure();
        botStructure.setDescription(TestDataHelper.stringOfLength(640));
        this.postFilter.validateBotStructure(botStructure);
    }

    @Test
    public void validateIntent_OK() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_LongName() throws ParameterValidationException {
        ApiIntent intent = createIntent(TestDataHelper.stringOfLength(251));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_NoUserExpressions() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setUserSays(Collections.EMPTY_LIST);
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_LongUserExpression() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setUserSays(Collections.singletonList(TestDataHelper.stringOfLength(251)));
        this.postFilter.validateIntent(intent);
    }

    @Test
    public void validateIntent_DuplicateUserExpression() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setUserSays(Arrays.asList("dupe", "dupe "));
        this.postFilter.validateIntent(intent);
        Assert.assertEquals(1, intent.getUserSays().size());
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_NoUserExpressions_Empty() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setUserSays(Collections.singletonList(""));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_NoResponses() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setResponses(Collections.EMPTY_LIST);
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_NoResponses_Empty() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setResponses(Collections.singletonList(""));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_LongResponse() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setResponses(Collections.singletonList(TestDataHelper.stringOfLength(251)));
        this.postFilter.validateIntent(intent);
    }

    @Test
    public void validateIntent_DuplicateResponse() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.setResponses(Arrays.asList("dupe", "dupe "));
        this.postFilter.validateIntent(intent);
        Assert.assertEquals(1, intent.getResponses().size());
    }

    @Test
    public void validateIntent_Variable_OK() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("entity", "label", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_NoEntity() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("", "label", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_NoLabel() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("name", "", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_RequiredNoPrompts() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        IntentVariable intentVariable = createRequiredIntentVariable("name", "label", 1);
        intentVariable.setPrompts(Collections.EMPTY_LIST);
        intent.addVariable(intentVariable);
        this.postFilter.validateIntent(intent);
    }

    @Test
    public void validateIntent_Variable_NotRequiredNoPrompts() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        IntentVariable intentVariable = createOptionalIntentVariable("name", "label", 1);
        intentVariable.setPrompts(Collections.EMPTY_LIST);
        intent.addVariable(intentVariable);
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_LongEntity() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable(TestDataHelper.stringOfLength(251),
                "label", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_LongLabel() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("entity",
                TestDataHelper.stringOfLength(251), 1));
        this.postFilter.validateIntent(intent);
    }

    @Test
    public void validateIntent_Variable_Label() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("entity", " !\"$%^&*()-_=+{[}]@'~#?/>.<,|\\", 1));
        intent.addVariable(createRequiredIntentVariable("entity2", " asdas1 623916293", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_DuplicateLabel() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("entity", "dupe", 1));
        intent.addVariable(createRequiredIntentVariable("entity2", "dupe ", 1));
        this.postFilter.validateIntent(intent);
    }

    @Test(expected = ParameterValidationException.class)
    public void validateIntent_Variable_BadLabel() throws ParameterValidationException {
        ApiIntent intent = createIntent(null);
        intent.addVariable(createRequiredIntentVariable("entity", "昨夜のコンサートは最高でした。", 1));
        this.postFilter.validateIntent(intent);
    }

    private ApiIntent createIntent(String name) {
        ApiIntent intent = new ApiIntent(name == null ? "name" : name, "", "");
        intent.setResponses(Collections.singletonList("response"));
        intent.setUserSays(Collections.singletonList("usersays"));
        return intent;
    }

    private IntentVariable createIntentVariable(String entity_name, String label, int numPrompts, boolean required) {
        IntentVariable intentVariable = new IntentVariable(entity_name, TestDataHelper.DEVID_UUID,
                required, numPrompts, "value", true, label);
        intentVariable.addPrompt("prompt");
        return intentVariable;
    }

    private IntentVariable createRequiredIntentVariable(String entity_name, String label, int numPrompts) {
        return createIntentVariable(entity_name, label, numPrompts, true);
    }

    private IntentVariable createOptionalIntentVariable(String entity_name, String label, int numPrompts) {
        return createIntentVariable(entity_name, label, numPrompts, false);
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
        intent.addVariable(new IntentVariable(entity.getEntityName(), entity.getDevOwner(), false, 3,
                "valid value", false, label));
        intents.add(intent);
        BotStructure structure = new BotStructure("Valid Name", "Valid Description", intents,
                "hello\nhi", entities, 1, true, 0, 0.5f,
                1, "en-US", "Europe/London", new ArrayList<>());
        return structure;
    }
}
