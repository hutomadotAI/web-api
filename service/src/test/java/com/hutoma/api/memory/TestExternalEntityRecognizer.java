package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.containers.sub.RecognizedEntity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 06/10/16.
 */
public class TestExternalEntityRecognizer {
    private ExternalEntityRecognizer recognizer;
    private EntityRecognizerService fakeService;

    @Before
    public void setup() {
        this.fakeService = mock(EntityRecognizerService.class);
        this.recognizer = new ExternalEntityRecognizer(this.fakeService, mock(ILogger.class));
    }

    @Test
    public void testExternal_recognizeOneEntity() {
        when(this.fakeService.getEntities(any())).thenReturn(Collections.emptyList());
        TestSimpleEntityRecognizer.recognizeOneEntity(this.recognizer);
    }

    @Test
    public void testExternal_recognizeMultipleEntities() {
        when(this.fakeService.getEntities(any())).thenReturn(Collections.emptyList());
        TestSimpleEntityRecognizer.recognizeMultipleEntities(this.recognizer);
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
        List<RecognizedEntity> systemEntities = new ArrayList<RecognizedEntity>() {{
            this.add(new RecognizedEntity("sys.var1", "system1"));
            this.add(new RecognizedEntity("sys.var2", "system2"));
        }};


        when(this.fakeService.getEntities(any())).thenReturn(systemEntities);

        List<Pair<String, String>> r = this.recognizer.retrieveEntities(
                String.format("CustomEntities %s and %s and SystemEntities %s and %s",
                        varValues[1], varValues[0],
                        systemEntities.get(0).getValue(), systemEntities.get(0).getValue()),
                l);
        Assert.assertEquals(4, r.size());
        // Note - the order is currently defined by the order on the MemoryVariable list,
        // and not on the string being parsed
        int index = 0;
        for (int i = 0; i < varNames.length; i++) {
            Assert.assertEquals(varNames[index], r.get(index).getA());
            Assert.assertEquals(varValues[index], r.get(index).getB());
            index++;
        }
        for (int i = 0; i < systemEntities.size(); i++) {
            Assert.assertEquals(systemEntities.get(i).getCategory(), r.get(index).getA());
            Assert.assertEquals(systemEntities.get(i).getValue(), r.get(index).getB());
            index++;
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
        when(this.fakeService.getEntities(any())).thenReturn(systemEntities);
        List<Pair<String, String>> r = this.recognizer.retrieveEntities(
                String.format("string with number %d", number), Collections.emptyList());
        Assert.assertEquals(1, r.size());
        Assert.assertEquals("sys.number", r.get(0).getA());
        Assert.assertEquals(number, Integer.parseInt(r.get(0).getB()));
    }
}
