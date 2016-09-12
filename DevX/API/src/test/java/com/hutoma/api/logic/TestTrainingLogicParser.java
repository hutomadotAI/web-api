package com.hutoma.api.logic;

import io.jsonwebtoken.lang.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by David MG on 10/08/2016.
 */
public class TestTrainingLogicParser {

    TrainingLogic logic;

    @Before
    public void setup() {
        logic = new TrainingLogic(null, null, null, null, null, null, null, null);
    }

    private String parse(String[] input) {
        return logic.parseTrainingFile(Collections.arrayToList(input)).replace('\n', '^');
    }

    @Test
    public void testParse_Pair() {
        Assert.assertEquals("H^A^^", parse(new String[]{ "H", "A"}));
    }

    @Test
    public void testParse_TwoPair() {
        Assert.assertEquals("H^A^^H^A^^", parse(new String[]{ "H", "A", "", "H", "A"}));
    }

    @Test
    public void testParse_Exchange_TwoPairs() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^^", parse(new String[]{ "H1", "A1", "H2", "A2" }));
    }

    @Test
    public void testParse_Exchange_OddNumber() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^[A2] H3^^", parse(new String[]{ "H1", "A1", "H2", "A2", "H3" }));
    }

    @Test
    public void testParse_Pair_After_EvenExchange() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^^H10^A11^^", parse(new String[]{ "H1", "A1", "H2", "A2", "", "H10", "A11" }));
    }

    @Test
    public void testParse_Pair_After_OddExchange() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^[A2] H3^^H10^A11^^", parse(new String[]{ "H1", "A1", "H2", "A2", "H3", "", "H10", "A11" }));
    }

    @Test
    public void testParse_Exchange_After_Pair() {
        Assert.assertEquals("H10^A11^^H1^A1^[A1] H2^A2^^", parse(new String[]{ "H10", "A11", "", "H1", "A1", "H2", "A2" }));
    }

    @Test
    public void testParse_Empty() {
        Assert.assertEquals("", parse(new String[]{ "" }));
    }

    @Test
    public void testParse_NoInput() {
        Assert.assertEquals("", parse(new String[]{ }));
    }
}
