package com.hutoma.api.memory;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.RecognizedEntity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.containers.sub.MemoryVariable;

import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 06/10/16.
 */
public class TestSimpleEntityRecognizer {
    private SimpleEntityRecognizer recognizer;

    @Before
    public void setup() {
        this.recognizer = new SimpleEntityRecognizer(mock(ILogger.class));
    }

    @Test
    public void testRecognizeOneEntity() {
        recognizeOneEntity(recognizer, "NAME", "VALUE");
    }

    @Test
    public void testRecognizeMultipleEntities() {
        final String[] varNames = {"var1", "var2"};
        final String[] varValues = {"value1", "value2"};
        recognizeMultipleEntities(recognizer, varNames, varValues);
    }

    public static void recognizeOneEntity(final IEntityRecognizer recognizer, final String variableName, final String variableValue) {
        List<MemoryVariable> l = new ArrayList<MemoryVariable>() {{
            this.add(new MemoryVariable(variableName, Arrays.asList(variableValue, "another value")));
        }};

        ChatRequestInfo fakeChatInfo = mock(ChatRequestInfo.class, Mockito.RETURNS_DEEP_STUBS);
        when(fakeChatInfo.getQuestion()).thenReturn("this is a " + variableValue + " to recognize");
        when(fakeChatInfo.getAiIdentity().getLanguage()).thenReturn(SupportedLanguage.EN);

        List<Pair<String, String>> r = recognizer.retrieveEntities(fakeChatInfo, l);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(variableName, r.get(0).getA());
        Assert.assertEquals(variableValue, r.get(0).getB());
    }

    public static void recognizeMultipleEntities(final IEntityRecognizer recognizer, final String[] varNames, final String[] varValues) {
        List<MemoryVariable> l = new ArrayList<>();
        for (int i = 0; i < varNames.length; i++) {
            l.add(new MemoryVariable(varNames[i], Arrays.asList("A", varValues[i], "B")));
        }
        l.add(new MemoryVariable("some other", Arrays.asList("X", "Y")));

        ChatRequestInfo fakeChatInfo = mock(ChatRequestInfo.class, Mockito.RETURNS_DEEP_STUBS);
        when(fakeChatInfo.getQuestion()).thenReturn(
                "Start " + varValues[1].toUpperCase() + " and " + varValues[0] + " end");
        when(fakeChatInfo.getAiIdentity().getLanguage()).thenReturn(SupportedLanguage.EN);

        List<Pair<String, String>> r = recognizer.retrieveEntities(fakeChatInfo, l);
        Assert.assertEquals(2, r.size());
        // Note - the order is currently defined by the order on the MemoryVariable list,
        // and not on the string being parsed
        for (int i = 0; i < varNames.length; i++) {
            Assert.assertEquals(varNames[i], r.get(i).getA());
            Assert.assertEquals(varValues[i], r.get(i).getB());
        }
    }
}
