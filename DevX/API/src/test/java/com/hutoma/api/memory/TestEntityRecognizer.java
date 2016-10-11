package com.hutoma.api.memory;

import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Pair;
import com.hutoma.api.containers.sub.MemoryVariable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Created by pedrotei on 06/10/16.
 */
public class TestEntityRecognizer {
    private SimpleEntityRecognizer recognizer;

    @Before
    public void setup() {
        this.recognizer = new SimpleEntityRecognizer(mock(Logger.class));
    }

    @Test
    public void testRecognizeOneEntity() {
        final String variableName = "NAME";
        final String variableValue = "VARIABLE";
        List<MemoryVariable> l = new ArrayList<MemoryVariable>() {{
            this.add(new MemoryVariable(variableName, Arrays.asList(variableValue, "another value")));
        }};
        List<Pair<String, String>> r = this.recognizer.retrieveEntities("this is a " + variableValue + " to recognize", l);
        Assert.assertEquals(1, r.size());
        Assert.assertEquals(variableName, r.get(0).getA());
        Assert.assertEquals(variableValue, r.get(0).getB());
    }

    @Test
    public void testRecognizeMultipleEntities() {
        final String[] varNames = {"var1", "var2"};
        final String[] varValues = {"value 1", "value2"};
        List<MemoryVariable> l = new ArrayList<MemoryVariable>() {{
            this.add(new MemoryVariable(varNames[0], Arrays.asList("A", varValues[0], "B")));
            this.add(new MemoryVariable(varNames[1], Arrays.asList(varValues[1], "K")));
            this.add(new MemoryVariable("some other", Arrays.asList("X", "Y")));
        }};
        List<Pair<String, String>> r = this.recognizer.retrieveEntities(
                "Start " + varValues[1].toUpperCase() + " and " + varValues[0] + " end", l);
        Assert.assertEquals(2, r.size());
        // Note - the order is currently defined by the order on the MemoryVariable list,
        // and not on the string being parsed
        for (int i = 0; i < varNames.length; i++) {
            Assert.assertEquals(varNames[i], r.get(i).getA());
            Assert.assertEquals(varValues[i], r.get(i).getB());
        }
    }
}
