package com.hutoma.api.connectors;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * Created by David MG on 11/08/2016.
 */
public class HTMLExtractor {

    public String getTextFromUrl(String url) throws HtmlExtractionException {
        String result;
        try {
            result = ArticleExtractor.INSTANCE.getText(url);
            if ((null == result) || (result.isEmpty())) {
                throw new HtmlExtractionException(new Exception("HTTP Fetch returned empty content"));
            }
        } catch (Exception e) {
            throw new HtmlExtractionException(e);
        }
        return result;
    }

    public static class HtmlExtractionException extends Exception {

        public HtmlExtractionException(Throwable cause) {
            super(cause);
        }
    }
}
