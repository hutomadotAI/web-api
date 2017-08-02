package com.hutoma.api.containers.facebook;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacebookNotification {

    @SerializedName("object")
    public String object;
    @SerializedName("entry")
    public List<Entry> entryList = null;

    public List<Entry> getEntryList() {
        return this.entryList;
    }

    public static class Messaging {

        @SerializedName("sender")
        private Sender sender;
        @SerializedName("recipient")
        private Recipient recipient;
        @SerializedName("timestamp")
        private long timestamp;
        @SerializedName("message")
        private Message message;
        @SerializedName("postback")
        private Postback postback;

        public Messaging(final String sender, final String recipient, final String message) {
            this.sender = new Sender();
            this.sender.id = sender;
            this.recipient = new Recipient();
            this.recipient.id = recipient;
            this.message = new Message();
            this.message.text = message;
        }

        public String getSender() {
            return Strings.nullToEmpty(this.sender.id);
        }

        public String getRecipient() {
            return Strings.nullToEmpty(this.recipient.id);
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public long getMessageSeq() {
            return this.message.seq;
        }

        public String getMessageText() {
            return Strings.nullToEmpty(this.message.text);
        }

        public boolean isMessage() {
            return this.message != null;
        }

        public boolean isPostback() {
            return this.postback != null;
        }

        public String getPostbackPayload() {
            return Strings.nullToEmpty(this.postback.payload);
        }

        public String getPostbackTitle() {
            return Strings.nullToEmpty(this.postback.title);
        }

        public String getPostbackReferral() {
            return (this.postback.referral == null) ? "" :
                    Strings.nullToEmpty(this.postback.referral.referrer);
        }

        static class Sender {
            @SerializedName("id")
            public String id;
        }

        static class Recipient {
            @SerializedName("id")
            public String id;
        }

        static class Message {
            @SerializedName("mid")
            public String mid;
            @SerializedName("seq")
            public long seq;
            @SerializedName("text")
            public String text;
        }

        static class Postback {
            @SerializedName("title")
            public String title;
            @SerializedName("payload")
            public String payload;
            @SerializedName("referral")
            public Referral referral;
        }

        static class Referral {
            @SerializedName("ref")
            public String referrer;
            @SerializedName("source")
            public String source;
            @SerializedName("type")
            public String referralType;
        }
    }

    public static class Entry {

        @SerializedName("id")
        private String id;
        @SerializedName("time")
        private long time;
        @SerializedName("messaging")
        private List<Messaging> messaging = null;

        public List<Messaging> getMessaging() {
            return this.messaging;
        }

        public boolean isMessagingEntry() {
            return (this.messaging != null) && (!this.messaging.isEmpty());
        }
    }

}
