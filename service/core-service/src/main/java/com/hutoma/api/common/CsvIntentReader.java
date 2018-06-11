package com.hutoma.api.common;

import com.hutoma.api.containers.ApiCsvImportResult;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.logging.ILogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class CsvIntentReader {

    private static final String LOGFROM = "vscintentreader";
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static final char DEFAULT_CELLLIST_SEPARATOR = ';';
    private static final String LINE_SEPARATOR_PATTERN = "\\R";
    private final ILogger logger;
    private char separator;
    private char quote;
    private char cellListSeparator;

    @Inject
    CsvIntentReader(final ILogger logger) {
        this.logger = logger;
        this.separator = DEFAULT_SEPARATOR;
        this.quote = DEFAULT_QUOTE;
        this.cellListSeparator = DEFAULT_CELLLIST_SEPARATOR;
    }

    public ApiCsvImportResult parseIntents(final String csvFile) {
        ApiCsvImportResult result = new ApiCsvImportResult();
        if (csvFile != null && !csvFile.isEmpty()) {
            String[] lines = splitLines(csvFile);
            for (int i = 0; i < lines.length; i++) {
                ApiIntent intent = parseLine(lines[i].trim());
                if (intent != null) {
                    result.addImported(intent);
                } else {
                    result.addWarning(String.format("No valid data to import at line %d", i + 1));
                }
            }
        }

        return result;
    }

    String[] splitLines(final String csvFile) {
        return csvFile.split(LINE_SEPARATOR_PATTERN);
    }

    public char getSeparator() {
        return this.separator;
    }

    public void setSeparator(final char separator) {
        this.separator = separator;
    }

    public char getQuoteChar() {
        return this.quote;
    }

    public void setQuoteChar(final char quoteChar) {
        this.quote = quoteChar;
    }

    ApiIntent parseLine(final String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        List<String> cells = new ArrayList<>();
        StringBuffer currentValue = new StringBuffer();
        boolean insideQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = line.toCharArray();
        for (char ch : chars) {
            if (insideQuotes) {
                startCollectChar = true;
                if (ch == this.quote) {
                    insideQuotes = false;
                    doubleQuotesInColumn = false;
                } else {
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            currentValue.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        currentValue.append(ch);
                    }
                }
            } else {
                if (ch == this.quote) {
                    insideQuotes = true;
                    if (chars[0] != '"' && this.quote == '\"') {
                        currentValue.append('"');
                    }
                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        currentValue.append('"');
                    }
                } else if (ch == this.separator) {
                    cells.add(currentValue.toString());
                    currentValue = new StringBuffer();
                    startCollectChar = false;

                } else {
                    currentValue.append(ch);
                }
            }
        }

        if (currentValue.length() > 0) {
            cells.add(currentValue.toString());
        }

        if (cells.size() < 3) {
            return null;
        } else {
            ApiIntent intent = new ApiIntent(cells.get(0), "", "");
            getSeparatedStrings(cells.get(1), this.cellListSeparator).forEach(intent::addUserSays);
            getSeparatedStrings(cells.get(2), this.cellListSeparator).forEach(intent::addResponse);
            return intent;
        }
    }

    private static List<String> getSeparatedStrings(final String str, final char separator) {
        return Arrays.asList(str.split(String.format("%c", separator)));
    }
}
