package com.hutoma.api.containers.facebook;

/***
 * Encapsulates a single message that we send to Facebook.
 * One or more of these could be a single complex message that results from a chat interaction.
 * For example, we might send two text messages to Facebook if the chat-response is more than
 * the allowed 640 chars
 */
public abstract class FacebookResponseSegment {

    public FacebookRichContentNode getRichContentNode() {
        return null;
    }

    public String getText() {
        return null;
    }

    public abstract boolean isRichContentSegment();

    public static class FacebookResponseTextSegment extends FacebookResponseSegment {

        private String text;

        public FacebookResponseTextSegment(final String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return this.text;
        }

        @Override
        public boolean isRichContentSegment() {
            return false;
        }
    }

    public static class FacebookResponseRichSegment extends FacebookResponseSegment {

        private FacebookRichContentNode richNode;

        public FacebookResponseRichSegment(final FacebookRichContentNode richNode) {
            this.richNode = richNode;
        }

        @Override
        public FacebookRichContentNode getRichContentNode() {
            return this.richNode;
        }

        @Override
        public boolean isRichContentSegment() {
            return true;
        }
    }
}

