package com.hutoma.api.logic;

import com.hutoma.api.common.ResultEvent;
import com.hutoma.api.containers.sub.ResultEventList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Training File parsing result object.
 */
final class TrainingFileParsingResult {

    private String trainingText;
    private ResultEventList events = new ResultEventList();

    /**
     * Gets the training text (line separated).
     * @return the training text
     */
    String getTrainingText() {
        return this.trainingText;
    }

    /**
     * Sets the training text (line separated).
     * @param text the training text
     */
    void setTrainingText(final String text) {
        this.trainingText = text;
    }

    /**
     * Gets the number of events.
     * @return the number of events
     */
    int getEventCount() {
        return this.events.size();
    }

    /**
     * Gets the events for a given eventType
     * @param eventType the event type
     * @return the list of events
     */
    List<String> getEventsFor(ResultEvent eventType) {
        return events.stream()
                .filter(x -> x.getKey() == eventType)
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Gets the event type for an event on a given position.
     * @param eventIndex the event position
     * @return the event type
     */
    ResultEvent getEventType(final int eventIndex) {
        return this.events.get(eventIndex).getKey();
    }

    /**
     * Gets the event text for an event on a given position.
     * @param eventIndex the event position
     * @return the event text
     */
    String getEventText(final int eventIndex) {
        return this.events.get(eventIndex).getValue();
    }

    /**
     * Gets all the events.
     * @return the list of all the events
     */
    ResultEventList getEvents() {
        return this.events;
    }

    /**
     * Adds a new event.
     * @param eventType the event type
     * @param text the event text
     */
    void addEvent(final ResultEvent eventType, final String text) {
        events.addEvent(eventType, text);
    }

    /**
     * Gets whether it has fatal events or not.
     * Fatal events are events which prevent the file to be used for training.
     * @return whether it has fatal events or not
     */
    boolean hasFatalEvents() {
        return this.events.stream().anyMatch(e ->
                e.getKey() == ResultEvent.UPLOAD_NO_CONTENT);
    }

}