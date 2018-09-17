package com.hutoma.api.common;

import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestBotStructureSerializer {

    private DatabaseAI fakeDatabaseAi;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;

    @Before
    public void setup() {
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
    }

    @Test
    public void testBotStructureSerializer() throws DatabaseException {
        final String trainingFile = "training file";
        final List<String> intentNames = Arrays.asList("intent1", "intent2");
        final UUID aiid = UUID.fromString(getSampleAI().getAiid());
        final ApiIntent intent1 = new ApiIntent(intentNames.get(0), "", "");
        final String webhookEndpoint = "http://endpoint";
        intent1.setWebHook(new WebHook(aiid, intentNames.get(0), webhookEndpoint, true));
        final IntentVariable intentVariable = new IntentVariable("entityname", DEVID_UUID, true, 1, "value",
                false, "label", false);
        final ApiIntent intent2 = new ApiIntent(intentNames.get(1), "", "");
        intent2.addVariable(intentVariable);
        final AiBot linkedBot = new AiBot(DEVID_UUID, AIID, BOTID, "botname", "desc", "longdesc", "alertmsg", "badge", new BigDecimal(1),
                "sample", "category", "licensetype", DateTime.now(), "privacy", "classification", "version",
                "video", AiBot.PublishingState.PUBLISHED, AiBot.PublishingType.SKILL, "icon");

        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(trainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(intentNames);
        when(this.fakeDatabaseEntitiesIntents.getIntent(aiid, intentNames.get(0))).thenReturn(intent1);
        when(this.fakeDatabaseEntitiesIntents.getIntent(aiid, intentNames.get(1))).thenReturn(intent2);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(linkedBot));

        BotStructure result = BotStructureSerializer.serialize(DEVID_UUID, AIID, this.fakeDatabaseAi,
                this.fakeDatabaseEntitiesIntents, new JsonSerializer());

        Assert.assertEquals(trainingFile, result.getTrainingFile());
        Assert.assertEquals(intentNames.size(), result.getIntents().size());
        // 1st intent
        Assert.assertEquals(intent1.getIntentName(), result.getIntents().get(0).getIntentName());
        Assert.assertTrue(result.getIntents().get(0).getWebHook().isEnabled());
        Assert.assertEquals(0, result.getIntents().get(0).getVariables().size());
        Assert.assertEquals(webhookEndpoint, result.getIntents().get(0).getWebHook().getEndpoint());
        // 2nd intent
        Assert.assertEquals(intent2.getIntentName(), result.getIntents().get(1).getIntentName());
        Assert.assertNull(result.getIntents().get(1).getWebHook());
        Assert.assertEquals(1, result.getIntents().get(1).getVariables().size());
        Assert.assertEquals(intentVariable.getEntityName(), result.getIntents().get(1).getVariables().get(0).getEntityName());

        Assert.assertEquals(BOTID, (int)result.getLinkedSkills().get(0));
    }
}
