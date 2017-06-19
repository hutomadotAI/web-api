package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacebookNotification {

    @SerializedName("object")
    public String object;
    @SerializedName("entry")
    public List<Entry> entry = null;

    private class Entry {

        @SerializedName("id")
        public String id;
        @SerializedName("time")
        public long time;
        @SerializedName("messaging")
        public List<Messaging> messaging = null;
    }

    private class Messaging {

        @SerializedName("sender")
        public Sender sender;
        @SerializedName("recipient")
        public Recipient recipient;
        @SerializedName("timestamp")
        public long timestamp;
        @SerializedName("message")
        public Message message;
    }

    private class Sender {
        @SerializedName("id")
        public String id;
    }

    private class Recipient {
        @SerializedName("id")
        public String id;
    }

    private class Message {
        @SerializedName("mid")
        public String mid;
        @SerializedName("seq")
        public long seq;
        @SerializedName("text")
        public String text;
    }

}
