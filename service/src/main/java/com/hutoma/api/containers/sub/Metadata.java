package com.hutoma.api.containers.sub;

/**
 * Created by David MG on 16/08/2016.
 */
public class Metadata {

    private String emotion;
    private String topic;

    public void setEmotion(final String emotion) {
        this.emotion = emotion;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getEmotion() {
        return this.emotion;
    }

    public String getTopic() {
        return this.topic;
    }


}
