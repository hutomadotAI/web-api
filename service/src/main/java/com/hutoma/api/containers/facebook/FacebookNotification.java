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

    public enum AttachmentType {
        location
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

        public boolean isQuickReply() {
            return (this.message.quickReply != null
                    && this.message.quickReply.payload != null);
        }

        public String getQuickReplyPayload() {
            return this.message.quickReply.payload;
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

        /***
         * https://developers.facebook.com/docs/messenger-platform/send-api-reference/quick-replies
         * @return
         */
        public FacebookLocation getFacebookLocation() {
            if (!isMessage()
                    || this.message.attachments == null
                    || this.message.attachments.isEmpty()) {
                return null;
            }
            Attachment attachment = this.message.attachments.get(0);
            if (attachment.attachmentType == null
                    || attachment.attachmentType != AttachmentType.location
                    || attachment.locationPayload == null
                    || attachment.locationPayload.coordinates == null) {
                return null;
            }
            return new FacebookLocation(
                    attachment.title,
                    attachment.locationPayload.coordinates.latitude,
                    attachment.locationPayload.coordinates.longitude);
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
            @SerializedName("attachments")
            public List<Attachment> attachments;
            @SerializedName("quick_reply")
            public QuickReply quickReply;
        }

        static class Attachment {
            @SerializedName("title")
            public String title;
            @SerializedName("url")
            public String url;
            @SerializedName("type")
            public AttachmentType attachmentType;
            @SerializedName("payload")
            public LocationPayload locationPayload;
        }

        static class LocationPayload {
            @SerializedName("coordinates")
            public Coordinates coordinates;
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

        private static class Coordinates {
            @SerializedName("lat")
            public Double latitude;
            @SerializedName("long")
            public Double longitude;
        }

        private static class QuickReply {
            @SerializedName("payload")
            public String payload;
        }
    }

    public static class FacebookLocation {

        final String title;
        final double latitude;
        final double longitude;

        public FacebookLocation(final String title, final double latitude, final double longitude) {
            this.title = title;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return String.format("%.6f, %.6f %s", latitude, longitude, title);
        }

        public String toLatLon() {
            return String.format("%f,%f", latitude, longitude);
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
