package com.hutoma.api.common;

import com.hutoma.api.containers.ApiCsvImportResult;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.validation.ParameterValidationException;
import com.hutoma.api.validation.Validate;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class TestCsvIntentReader {

    private ILogger fakeLogger;
    private Validate fakeValidate;

    public TestCsvIntentReader() {
        this.fakeLogger = mock(ILogger.class);
        this.fakeValidate = mock(Validate.class);
    }

    @Test
    public void testCsvReader_canSetAndGetSeparator() {
        CsvIntentReader csv = getReader();
        csv.setSeparator('+');
        Assert.assertEquals('+', csv.getSeparator());
    }

    @Test
    public void testCsvReader_canSetAndGetQuoteChar() {
        CsvIntentReader csv = getReader();
        csv.setQuoteChar('+');
        Assert.assertEquals('+', csv.getQuoteChar());
    }

    @Test
    public void testCsvReader_linesSplitUnix() {
        testCsvReader_lineSeparator("\n");
    }

    @Test
    public void testCsvReader_linesSplitWindows() {
        testCsvReader_lineSeparator("\r\n");
    }

    @Test
    public void testCsvReader_noLines() {
        ApiCsvImportResult result = getReader().parseIntents("");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertTrue(result.getWarnings().isEmpty());
        Assert.assertTrue(result.getErrors().isEmpty());
    }
    @Test
    public void testCsvReader_nullLines() {
        ApiCsvImportResult result = getReader().parseIntents(null);
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertTrue(result.getWarnings().isEmpty());
        Assert.assertTrue(result.getErrors().isEmpty());
    }

    @Test
    public void testCsvReader_emptyLines() {
        ApiCsvImportResult result = getReader().parseIntents("\n");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertTrue(result.getWarnings().isEmpty());
        Assert.assertTrue(result.getErrors().isEmpty());
    }

    @Test
    public void testCsvReader_linesOnlyWithSpaces() {
        // Add 3 empty lines
        ApiCsvImportResult result = getReader().parseIntents(" \n \n ");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertEquals(3, result.getWarnings().size());
        Assert.assertTrue(result.getErrors().isEmpty());
    }

    @Test
    public void testCsvReader_quotedValue() {
        CsvIntentReader csv = getReader();
        ApiIntent intent = csv.parseLine("\"name,alsoname\",us,r,");
        Assert.assertEquals("name,alsoname", intent.getIntentName());
    }

    @Test
    public void testCsvReader_doubleQuotedValue() {
        CsvIntentReader csv = getReader();
        ApiIntent intent = csv.parseLine("name\"\"thename,us,r,");
        Assert.assertEquals("name\"thename", intent.getIntentName());
    }

    @Test
    public void testCsvReader_doubleQuotedValue_insideQuotes() {
        CsvIntentReader csv = getReader();
        ApiIntent intent = csv.parseLine("\"name\"\"thename\",us,r,");
        Assert.assertEquals("name\"thename", intent.getIntentName());
    }

    @Test
    public void testCsvReader_doubleQuotedValue_insideCustomQuotes() {
        CsvIntentReader csv = getReader();
        csv.setQuoteChar('*');
        ApiIntent intent = csv.parseLine("*name\"\"thename*,us,r,");
        Assert.assertEquals("name\"thename", intent.getIntentName());
    }

    @Test
    public void testCsvReader_nonTerminatedQuotes() {
        // non-terminated quotes prevents the intent from being read properly
        Assert.assertNull(getReader().parseLine("\"name,us,r,"));
    }

    @Test
    public void testCsvReader_customSeparator() {
        CsvIntentReader csv = getReader();
        csv.setSeparator('*');
        ApiIntent intent = csv.parseLine("name*us*r*");
        Assert.assertEquals("name", intent.getIntentName());
        Assert.assertEquals("us", intent.getUserSays().get(0));
        Assert.assertEquals("r", intent.getResponses().get(0));
    }

    @Test
    public void testCsvReader_getListFromCell() {
        CsvIntentReader csv = getReader();
        ApiIntent intent = csv.parseLine("name,us1;us2,r1;r2,");
        Assert.assertEquals("name", intent.getIntentName());
        Assert.assertEquals("us1", intent.getUserSays().get(0));
        Assert.assertEquals("us2", intent.getUserSays().get(1));
        Assert.assertEquals("r1", intent.getResponses().get(0));
        Assert.assertEquals("r2", intent.getResponses().get(1));
    }

    @Test
    public void testCsvReader_intentsAndInvalidLines() {
        when(this.fakeValidate.isIntentNameValid(anyString())).thenReturn(true);
        when(this.fakeValidate.areIntentResponsesValid(anyList())).thenReturn(true);
        when(this.fakeValidate.areIntentUserSaysValid(anyList())).thenReturn(true);
        ApiCsvImportResult result = getReader().parseIntents("name,us,r,\nNOT_VALID!\nname2,us2,r2");
        Assert.assertEquals(2, result.getImported().size());
        Assert.assertEquals(1, result.getWarnings().size());
        Assert.assertEquals("name", result.getImported().get(0).getIntent().getIntentName());
        Assert.assertEquals("name2", result.getImported().get(1).getIntent().getIntentName());
    }

    @Test
    public void testCsvReader_spacesTrimmed() {
        ApiIntent intent = getReader().parseLine("  name , us ; us2 , r ; r2 , ");
        Assert.assertEquals("name", intent.getIntentName());
        Assert.assertEquals("us", intent.getUserSays().get(0));
        Assert.assertEquals("us2", intent.getUserSays().get(1));
        Assert.assertEquals("r", intent.getResponses().get(0));
        Assert.assertEquals("r2", intent.getResponses().get(1));
    }

    @Test
    public void testCsvReader_intentsEmptyComponents() {
        String[] lines = {"name,,r", "name,;,r", "name,us,,", "name,us,;"};
        for (String line: lines) {
            ApiCsvImportResult result = getReader().parseIntents(line);
            Assert.assertTrue(result.getImported().isEmpty());
            Assert.assertEquals(1, result.getErrors().size());
        }
    }

    @Test
    public void testCsvReader_invalidIntentName() throws ParameterValidationException {
        doThrow(ParameterValidationException.class).when(this.fakeValidate).validateIntentName(anyString());
        ApiCsvImportResult result = getReader().parseIntents("name,us,r");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertEquals("Invalid intent name: name", result.getErrors().get(0).getError());
    }

    @Test
    public void testCsvReader_responsesNotValid() {
        when(this.fakeValidate.isIntentNameValid(anyString())).thenReturn(true);
        when(this.fakeValidate.areIntentResponsesValid(anyList())).thenReturn(false);
        ApiCsvImportResult result = getReader().parseIntents("name,us,r");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertEquals(1, result.getErrors().size());
    }

    @Test
    public void testCsvReader_userSaysNotValid() {
        when(this.fakeValidate.isIntentNameValid(anyString())).thenReturn(true);
        when(this.fakeValidate.areIntentResponsesValid(anyList())).thenReturn(true);
        when(this.fakeValidate.areIntentUserSaysValid(anyList())).thenReturn(false);
        ApiCsvImportResult result = getReader().parseIntents("name,us,r");
        Assert.assertTrue(result.getImported().isEmpty());
        Assert.assertEquals(1, result.getErrors().size());
    }

    private void testCsvReader_lineSeparator(final String lineSeparator) {
        final String line = "name, usersays, responses,";
        final int nLines = 3;
        CsvIntentReader csv = getReader();
        String[] lines = csv.splitLines(getLinesWithSeparator(line,lineSeparator, nLines));
        Assert.assertEquals(3, lines.length);
        for (int i = 0; i < nLines; i++) {
            Assert.assertEquals(line, lines[i]);
        }
    }

    private CsvIntentReader getReader() {
        return new CsvIntentReader(this.fakeLogger, this.fakeValidate);
    }

    private String getLinesWithSeparator(final String line, final String separator, final int nCopies) {
        return StringUtils.repeat(line + separator, nCopies);
    }
}
