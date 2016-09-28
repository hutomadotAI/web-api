package com.hutoma.api.logic;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static com.hutoma.api.logic.TrainingFileParsingResult.ParsingResultEvent.MISSING_RESPONSE;
import static com.hutoma.api.logic.TrainingFileParsingResult.ParsingResultEvent.NO_CONTENT;
import static junitparams.JUnitParamsRunner.$;


/**
 * Created by David MG on 10/08/2016.
 */
@RunWith(JUnitParamsRunner.class)
public class TestTrainingLogicParser {

    TrainingLogic logic;

    private static Object[] noResponseDataProvider() {
        return $(
                $("Q"),
                $("Q1", "R1", "Q"),
                $("Q", "", "Q1", "R1"),
                $("Q1", "R1", "", "Q", "", "Q2", "R2")
        );
    }

    @Before
    public void setup() {
        logic = new TrainingLogic(null, null, null, null, null, null, null, null);
    }

    private String parse(String[] input) {
        return logic.parseTrainingFile(Arrays.asList(input)).getTrainingText().replace('\n', '^');
    }

    @Test
    public void testParse_Pair() {
        Assert.assertEquals("H^A^^", parse(new String[]{"H", "A"}));
    }

    @Test
    public void testParse_noInputText() {
        TrainingFileParsingResult result = logic.parseTrainingFile(Arrays.asList("", "", ""));
        List<String> eventsForNoResponse = result.getEventsFor(NO_CONTENT);
        Assert.assertEquals(1, eventsForNoResponse.size());
        Assert.assertEquals("", result.getTrainingText());
    }

    @Test
    public void testParse_TwoPair() {
        Assert.assertEquals("H^A^^H^A^^", parse(new String[]{"H", "A", "", "H", "A"}));
    }

    @Test
    public void testParse_Exchange_TwoPairs() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^^", parse(new String[]{"H1", "A1", "H2", "A2"}));
    }

    @Test
    public void testParse_Exchange_OddNumber() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^^", parse(new String[]{"H1", "A1", "H2", "A2", "H3"}));
    }

    @Test
    public void testParse_Pair_After_EvenExchange() {
        Assert.assertEquals("H1^A1^[A1] H2^A2^^H10^A11^^", parse(new String[]{"H1", "A1", "H2", "A2", "", "H10", "A11"}));
    }

    @Test
    public void testParse_Pair_After_OddExchange() {
        // H3 is ignored as there was no response
        Assert.assertEquals("H1^A1^[A1] H2^A2^^H10^A11^^", parse(new String[]{"H1", "A1", "H2", "A2", "H3", "", "H10", "A11"}));
    }

    @Test
    public void testParse_Exchange_After_Pair() {
        Assert.assertEquals("H10^A11^^H1^A1^[A1] H2^A2^^", parse(new String[]{"H10", "A11", "", "H1", "A1", "H2", "A2"}));
    }

    @Test
    public void testParse_Empty() {
        Assert.assertEquals("", parse(new String[]{""}));
    }

    @Test
    public void testParse_NoInput() {
        Assert.assertEquals("", parse(new String[]{}));
    }

    @Test
    @Parameters(method = "noResponseDataProvider")
    public void testParse_NoResponse(final String[] textLines) {
        TrainingFileParsingResult result = logic.parseTrainingFile(Arrays.asList(textLines));
        List<String> eventsForNoResponse = result.getEventsFor(MISSING_RESPONSE);
        Assert.assertEquals(1, eventsForNoResponse.size());
        Assert.assertEquals("Q", eventsForNoResponse.get(0));
    }

    @Test
    public void testParse_NoResponse_multiple() {
        TrainingFileParsingResult result = logic.parseTrainingFile(Arrays.asList("Q1", "", "Q2", "", "Q3", "R3"));
        List<String> eventsForNoResponse = result.getEventsFor(MISSING_RESPONSE);
        Assert.assertEquals(2, eventsForNoResponse.size());
        Assert.assertEquals("Q1", eventsForNoResponse.get(0));
        Assert.assertEquals("Q2", eventsForNoResponse.get(1));
    }
}
