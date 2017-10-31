package com.hutoma.api.containers.facebook;

import com.hutoma.api.connectors.FacebookConnector;

import java.util.List;
import java.util.stream.Collectors;

/***
 * Encapsulates a single message that we send to Facebook.
 * One or more of these could be a single complex message that results from a chat interaction.
 * For example, we might send two text messages to Facebook if the chat-response is more than
 * the allowed 640 chars
 */
public abstract class FacebookResponseSegment {

    protected FacebookMessageNode messageNode;

    public void populateMessageContent(FacebookConnector.SendMessage message) {
        message.setMessageNode(this.messageNode);
    }

    public static class FacebookResponseTextSegment extends FacebookResponseSegment {

        public FacebookResponseTextSegment(final String text) {
            messageNode = new FacebookMessageNode()
                    .setText(text);
        }
    }

    public static class FacebookResponseAttachmentSegment extends FacebookResponseSegment {

        public FacebookResponseAttachmentSegment(final FacebookRichContentAttachment attachment) {
            this.messageNode = new FacebookMessageNode().setAttachment(attachment);
        }
    }

    public static class FacebookResponseQuickRepliesSegment extends FacebookResponseSegment {

        /***
         * Intent entity expansion using text only for quick reply buttons
         * @param question
         * @param options
         */
        public FacebookResponseQuickRepliesSegment(String question, List<String> options) {
            this.messageNode = new FacebookMessageNode()
                    .setText(question);
            this.messageNode.setQuickReplies(options.stream()
                    .map(name -> new FacebookQuickReply(name, name))
                    .collect(Collectors.toList()));
        }

        public FacebookResponseQuickRepliesSegment(List<FacebookQuickReply> quickReplies,
                                                   String question) {
            this.messageNode = new FacebookMessageNode()
                    .setText(question)
                    .setQuickReplies(quickReplies);
        }

        public FacebookResponseQuickRepliesSegment(List<FacebookQuickReply> quickReplies,
                                                   FacebookRichContentAttachment attachment) {
            this.messageNode = new FacebookMessageNode()
                    .setAttachment(attachment)
                    .setQuickReplies(quickReplies);
        }
    }
}

