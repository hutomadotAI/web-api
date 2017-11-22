package com.hutoma.api.containers.facebook;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FacebookMessengerProfile {

    private final FacebookMessengerProfileSet profileSet;
    private final FacebookMessengerProfileDelete profileDelete;

    private final boolean noGreeting;
    private final boolean noGetStarted;

    public FacebookMessengerProfile(String greeting, String getStarted) {

        this.profileSet = new FacebookMessengerProfileSet();
        this.profileDelete = new FacebookMessengerProfileDelete();

        if (this.noGreeting = fieldHasNoContent(greeting)) {
            this.profileDelete.toDelete.add("greeting");
        } else {
            this.profileSet.greeting = Collections.singletonList(
                    new Greeting("default", greeting));
        }

        if (this.noGetStarted = fieldHasNoContent(getStarted)) {
            this.profileDelete.toDelete.add("get_started");
        } else {
            this.profileSet.getStarted = new GetStarted(getStarted);
        }
    }

    public boolean isSetGreeting() {
        return !this.noGreeting;
    }

    public boolean isSetGetStarted() {
        return !this.noGetStarted;
    }

    public FacebookMessengerProfileSet getProfileSet() {
        return this.profileSet;
    }

    public FacebookMessengerProfileDelete getProfileDelete() {
        return this.profileDelete;
    }

    public static class FacebookMessengerProfileDelete {
        @SerializedName("fields")
        public List<String> toDelete;

        public FacebookMessengerProfileDelete() {
            this.toDelete = new ArrayList<>();
        }

        public boolean hasContent() {
            return !this.toDelete.isEmpty();
        }
    }

    public static class FacebookMessengerProfileSet {

        @SerializedName("greeting")
        public List<Greeting> greeting;
        @SerializedName("get_started")
        private GetStarted getStarted;

        public boolean hasContent() {
            return (this.greeting != null) || (this.getStarted != null);
        }
    }

    private static class GetStarted {

        @SerializedName("payload")
        private String payload;

        public GetStarted(final String payload) {
            this.payload = payload;
        }
    }

    private static class Greeting {
        @SerializedName("locale")
        private String locale;

        @SerializedName("text")
        private String text;

        public Greeting(final String locale, final String text) {
            this.locale = locale;
            this.text = text;
        }
    }

    protected boolean fieldHasNoContent(String field) {
        return Strings.isNullOrEmpty(field) || field.trim().isEmpty();
    }

}
