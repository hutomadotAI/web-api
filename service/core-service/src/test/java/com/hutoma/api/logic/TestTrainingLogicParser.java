package com.hutoma.api.logic;

import com.hutoma.api.containers.sub.ResultEvent;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static com.hutoma.api.containers.sub.ResultEvent.UPLOAD_MISSING_RESPONSE;
import static com.hutoma.api.containers.sub.ResultEvent.UPLOAD_NO_CONTENT;


/**
 * Created by David MG on 10/08/2016.
 */
@RunWith(DataProviderRunner.class)
public class TestTrainingLogicParser {

    private TrainingLogic logic;

    @DataProvider
    public static Object[] noResponseDataProvider() {
        return new Object[]{
                new String[]{"Q"},
                new String[]{"Q1", "R1", "Q"},
                new String[]{"Q", "", "Q1", "R1"},
                new String[]{"Q1", "R1", "", "Q", "", "Q2", "R2"}
        };
    }

    @Before
    public void setup() {
        this.logic = new TrainingLogic(null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void testParse_Pair() {
        Assert.assertEquals("H^A^^", parse(new String[]{"H", "A"}));
    }

    @Test
    public void testParse_noInputText() {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList("", "", ""));
        List<String> eventsForNoResponse = result.getEventsFor(UPLOAD_NO_CONTENT);
        Assert.assertEquals(1, eventsForNoResponse.size());
        Assert.assertEquals("", result.getTrainingText());
    }

    @Test
    public void testParse_TwoPair() {
        Assert.assertEquals(String.format("H^A%s^^H^A^^", ""), parse(new String[]{"H", "A", "", "H", "A"}));
    }

    @Test
    public void testParse_Exchange_TwoPairs() {
        Assert.assertEquals("H1^A1^H2^A2^^", parse(new String[]{"H1", "A1", "H2", "A2"}));
    }

    @Test
    public void testParse_Exchange_OddNumber() {
        Assert.assertEquals("H1^A1^H2^A2^^", parse(new String[]{"H1", "A1", "H2", "A2", "H3"}));
    }

    @Test
    public void testParse_Pair_After_EvenExchange() {
        Assert.assertEquals(
                String.format("H1^A1^H2^A2%s^^H10^A11^^", ""),
                parse(new String[]{"H1", "A1", "H2", "A2", "", "H10", "A11"}));
    }

    @Test
    public void testParse_Pair_After_OddExchange() {
        // H3 is ignored as there was no response
        Assert.assertEquals(String.format("H1^A1^H2^A2%s^^H10^A11^^", ""),
                parse(new String[]{"H1", "A1", "H2", "A2", "H3", "", "H10", "A11"}));
    }

    @Test
    public void testParse_Exchange_After_Pair() {
        Assert.assertEquals(
                String.format("H10^A11%s^^H1^A1^H2^A2^^", ""),
                parse(new String[]{"H10", "A11", "", "H1", "A1", "H2", "A2"}));
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
    @UseDataProvider("noResponseDataProvider")
    public void testParse_NoResponse(final String[] textLines) {
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList(textLines));
        List<String> eventsForNoResponse = result.getEventsFor(UPLOAD_MISSING_RESPONSE);
        Assert.assertEquals(1, eventsForNoResponse.size());
        Assert.assertEquals("Q", eventsForNoResponse.get(0));
    }

    @Test
    public void testParse_NoResponse_multiple() {
        // In separate conversations
        TrainingFileParsingResult result = this.logic.parseTrainingFile(Arrays.asList("Q1", "", "Q2", "", "Q3", "R3"));
        List<String> eventsForNoResponse = result.getEventsFor(UPLOAD_MISSING_RESPONSE);
        Assert.assertEquals(2, eventsForNoResponse.size());
        Assert.assertEquals("Q1", eventsForNoResponse.get(0));
        Assert.assertEquals("Q2", eventsForNoResponse.get(1));

        // Within a conversation and at the end
        result = this.logic.parseTrainingFile(Arrays.asList("Q1", "R1", "Q2", "", "Q3", "R3", "Q4", "", "Q5"));
        eventsForNoResponse = result.getEventsFor(UPLOAD_MISSING_RESPONSE);
        Assert.assertEquals(3, eventsForNoResponse.size());
        Assert.assertEquals("Q2", eventsForNoResponse.get(0));
        Assert.assertEquals("Q4", eventsForNoResponse.get(1));
        Assert.assertEquals("Q5", eventsForNoResponse.get(2));
    }

    @Test
    public void testTrainingFileParsingResult_events() {
        ResultEvent[] events = {ResultEvent.UPLOAD_MISSING_RESPONSE, ResultEvent.UPLOAD_NO_CONTENT};
        String[] eventText = {"event1", "event2"};
        TrainingFileParsingResult pr = new TrainingFileParsingResult();
        pr.addEvent(events[0], eventText[0]);
        pr.addEvent(events[1], eventText[1]);
        Assert.assertEquals(2, pr.getEvents().size());
        Assert.assertEquals(events[0], pr.getEventType(0));
        Assert.assertEquals(eventText[0], pr.getEventText(0));
        Assert.assertEquals(events[1], pr.getEventType(1));
        Assert.assertEquals(eventText[1], pr.getEventText(1));
        Assert.assertTrue(pr.hasFatalEvents());
    }

    @Test
    public void testTrainingFileParsingResult_fatalEvents() {
        ResultEvent[] events = {ResultEvent.UPLOAD_MISSING_RESPONSE, ResultEvent.UPLOAD_NO_CONTENT};
        TrainingFileParsingResult pr = new TrainingFileParsingResult();
        pr.addEvent(events[0], "");
        pr.addEvent(events[1], "");
        Assert.assertTrue(pr.hasFatalEvents());
    }

    @Test
    public void testTrainingFileParsingResult_noFatalEvents() {
        ResultEvent[] events = {ResultEvent.UPLOAD_MISSING_RESPONSE, ResultEvent.UPLOAD_MISSING_RESPONSE};
        TrainingFileParsingResult pr = new TrainingFileParsingResult();
        pr.addEvent(events[0], "");
        pr.addEvent(events[1], "");
        Assert.assertFalse(pr.hasFatalEvents());
    }

    private String parse(String[] input) {
        return this.logic.parseTrainingFile(Arrays.asList(input)).getTrainingText().replace('\n', '^');
    }
}
