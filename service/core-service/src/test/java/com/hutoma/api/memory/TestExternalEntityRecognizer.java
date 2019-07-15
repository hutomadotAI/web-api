package com.hutoma.api.memory;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RecognizedEntity;

import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 06/10/16.
 */
public class TestExternalEntityRecognizer {
    private ExternalEntityRecognizer recognizer;
    private EntityRecognizerService fakeService;
    private ChatRequestInfo fakeChatInfo;
    private FeatureToggler fakeFeatureToggler;

    @Before
    public void setup() {
        this.fakeService = mock(EntityRecognizerService.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);
        this.recognizer = new ExternalEntityRecognizer(this.fakeService, mock(ILogger.class), fakeFeatureToggler);
        this.fakeChatInfo = mock(ChatRequestInfo.class, Mockito.RETURNS_DEEP_STUBS);

        when(this.fakeFeatureToggler.getStateForAiid(any(), any(), any())).thenReturn(FeatureToggler.FeatureState.C);
    }

    @Test
    public void testExternal_injectedCtor() {
        ServiceLocator sl = Mockito.mock(ServiceLocator.class);
        ILogger logger = mock(ILogger.class);
        ExternalEntityRecognizer r = new ExternalEntityRecognizer(sl, logger, fakeFeatureToggler);
        Assert.assertEquals(logger, r.getLogger());
    }

    @Test
    public void testExternal_recognizeOneEntity() {
        final String varName = "NAME";
        final String varValue = "VALUE";
        when(this.fakeService.getEntities(any(), any())).thenReturn(Collections.singletonList(new RecognizedEntity(varName, varValue)));
        TestSimpleEntityRecognizer.recognizeOneEntity(this.recognizer, varName, varValue);
    }

    @Test
    public void testExternal_recognizeMultipleEntities() {
        final String[] varNames = {"var1", "var2"};
        final String[] varValues = {"value1", "value2"};
        when(this.fakeService.getEntities(any(), any())).thenReturn(Arrays.asList(
                new RecognizedEntity(varNames[0], varValues[0]),
                new RecognizedEntity(varNames[1], varValues[1])
        ));
        TestSimpleEntityRecognizer.recognizeMultipleEntities(this.recognizer, varNames, varValues);
    }

    @Test
    public void testExternal_recognizeMultipleEntities_andSystemEntities() {
        final String[] varNames = {"var1", "var2"};
        final String[] varValues = {"value1", "value2"};
        List<MemoryVariable> l = new ArrayList<MemoryVariable>() {{
            this.add(new MemoryVariable(varNames[0], Arrays.asList("A", varValues[0], "B")));
            this.add(new MemoryVariable(varNames[1], Arrays.asList(varValues[1], "K")));
            this.add(new MemoryVariable("some other", Arrays.asList("X", "Y")));
        }};
        List<RecognizedEntity> recognized = new ArrayList<RecognizedEntity>() {{
            this.add(new RecognizedEntity(varNames[0], varValues[0]));
            this.add(new RecognizedEntity(varNames[1], varValues[1]));
            this.add(new RecognizedEntity("sys.var1", "system1"));
            this.add(new RecognizedEntity("sys.var2", "system2"));
        }};


        when(this.fakeService.getEntities(any(), any())).thenReturn(recognized);
        when(this.fakeChatInfo.getQuestion()).thenReturn(
                String.format("CustomEntities %s and %s and SystemEntities %s and %s",
                        recognized.get(0).getCategory(),
                        recognized.get(1).getCategory(),
                        recognized.get(2).getCategory(),
                        recognized.get(3).getCategory()));
        when(this.fakeChatInfo.getAiIdentity().getLanguage()).thenReturn(SupportedLanguage.EN);

        List<Pair<String, String>> r = this.recognizer.retrieveEntities(
                fakeChatInfo,
                l);
        Assert.assertEquals(4, r.size());
        // Note - the order is currently defined by the order on the MemoryVariable list,
        // and not on the string being parsed
        for (int i = 0; i < recognized.size(); i++) {
            Assert.assertEquals(recognized.get(i).getCategory(), r.get(i).getA());
            Assert.assertEquals(recognized.get(i).getValue(), r.get(i).getB());
        }

    }

    @Test
    public void testGetNumbersFromString() {
        Assert.assertEquals(Collections.emptyList(), ExternalEntityRecognizer.getNumbersFromString(""));
        Assert.assertEquals(Collections.emptyList(), ExternalEntityRecognizer.getNumbersFromString("no numbers"));
        Assert.assertEquals(Collections.emptyList(), ExternalEntityRecognizer.getNumbersFromString("no1 numbers2"));
        Assert.assertEquals(Collections.singletonList("1"), ExternalEntityRecognizer.getNumbersFromString("1"));
        Assert.assertEquals(Collections.singletonList("123"), ExternalEntityRecognizer.getNumbersFromString("123"));
        Assert.assertEquals(Collections.singletonList("-1"), ExternalEntityRecognizer.getNumbersFromString("-1"));
        Assert.assertEquals(Collections.singletonList("1.000123"), ExternalEntityRecognizer.getNumbersFromString("1.000123"));
        Assert.assertEquals(Collections.singletonList("-1.000123"), ExternalEntityRecognizer.getNumbersFromString("-1.000123"));
        Assert.assertEquals(Arrays.asList("1", "0.123", "-5.9"), ExternalEntityRecognizer.getNumbersFromString("1 0.123 -5.9"));
        Assert.assertEquals(Arrays.asList("1", "0.123", "-5.9"), ExternalEntityRecognizer.getNumbersFromString("1 some text 0.123 more text -5.9 end"));
        // Don't process currency
        Assert.assertEquals(Collections.emptyList(), ExternalEntityRecognizer.getNumbersFromString("£123.45 €1.99 $9.87"));
    }

    @Test
    public void testExternal_discardExternalSysNumber() {
        final int number = 10;
        List<RecognizedEntity> systemEntities = new ArrayList<RecognizedEntity>() {{
            this.add(new RecognizedEntity("sys.number", Integer.toString(number + 1)));
        }};
        when(this.fakeService.getEntities(any(), any())).thenReturn(systemEntities);
        when(this.fakeChatInfo.getQuestion()).thenReturn(String.format("string with number %d", number));
        when(this.fakeChatInfo.getAiIdentity().getLanguage()).thenReturn(SupportedLanguage.EN);
        List<Pair<String, String>> r = this.recognizer.retrieveEntities(
                fakeChatInfo, Collections.emptyList());
        Assert.assertEquals(1, r.size());
        Assert.assertEquals("sys.number", r.get(0).getA());
        Assert.assertEquals(number, Integer.parseInt(r.get(0).getB()));
    }
}
