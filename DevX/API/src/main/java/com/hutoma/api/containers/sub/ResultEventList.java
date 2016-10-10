package com.hutoma.api.containers.sub;

import com.hutoma.api.common.ResultEvent;

import java.util.AbstractMap;
import java.util.ArrayList;

/***
 * List of key value pairs to describe events that occurred
 */
public class ResultEventList extends ArrayList<AbstractMap.SimpleEntry<ResultEvent, String>> {

    /***
     * Add a mapping from result event to a text description
     * @param eventType
     * @param text
     */
    public void addEvent(final ResultEvent eventType, final String text) {
        this.add(new AbstractMap.SimpleEntry<>(eventType, text));
    }
}
