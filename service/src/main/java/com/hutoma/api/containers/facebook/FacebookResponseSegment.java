package com.hutoma.api.containers.facebook;

import com.hutoma.api.connectors.FacebookConnector;

import java.util.List;

/***
 * Encapsulates a single message that we send to Facebook.
 * One or more of these could be a single complex message that results from a chat interaction.
 * For example, we might send two text messages to Facebook if the chat-response is more than
 * the allowed 640 chars
 */
public abstract class FacebookResponseSegment {

    public abstract void populateMessageContent(FacebookConnector.SendMessage message);

    public static class FacebookResponseTextSegment extends FacebookResponseSegment {

        private String text;

        public FacebookResponseTextSegment(final String text) {
            this.text = text;
        }

        @Override
        public void populateMessageContent(FacebookConnector.SendMessage message) {
            message.setText(this.text);
        }
    }

    public static class FacebookResponseRichSegment extends FacebookResponseSegment {

        private FacebookRichContentNode richNode;

        public FacebookResponseRichSegment(final FacebookRichContentNode richNode) {
            this.richNode = richNode;
        }

        @Override
        public void populateMessageContent(final FacebookConnector.SendMessage message) {
            message.setRichContent(this.richNode);
        }
    }

    public static class FacebookResponseQuickRepliesSegment extends FacebookResponseSegment {

        private List<String> options;
        private String text;

        public FacebookResponseQuickRepliesSegment(String question, List<String> options) {
            this.text = question;
            this.options = options;
        }

        @Override
        public void populateMessageContent(final FacebookConnector.SendMessage message) {
            message.setText(this.text);
            message.setQuickReplies(this.options);
        }
    }

}

