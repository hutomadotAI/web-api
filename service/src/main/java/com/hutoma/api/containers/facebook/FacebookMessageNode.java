package com.hutoma.api.containers.facebook;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/***
 * Dual-format support.
 * This class can deserialize as either a Node or an Attachment
 * The chat handler will figure it out at run-time.
 */
public class FacebookMessageNode extends FacebookRichContentAttachment {

    @SerializedName("quick-replies")
    private List<FacebookQuickReply> quickReplies;

    @SerializedName("attachment")
    private FacebookRichContentAttachment attachment;

    @SerializedName("text")
    private String text;

    public FacebookMessageNode() {
    }

    /***
     * Insert the extra new node into the tree
     * @param facebookNode
     */
    public FacebookMessageNode(final FacebookRichContentAttachment facebookNode) {
        this.attachment = facebookNode;
    }

    public String getText() {
        return text;
    }

    public FacebookMessageNode setText(final String text) {
        this.text = text;
        return this;
    }

    /***
     * If any of the fields defined in this class are non-null
     * then this is not the older format.
     * @return
     */
    public boolean isDeprecatedFormat() {
        return !hasAttachment() && !hasQuickReplies() && text == null;
    }

    public boolean hasAttachment() {
        return attachment != null;
    }

    public boolean hasQuickReplies() {
        return (quickReplies != null) && (!quickReplies.isEmpty());
    }

    public List<FacebookQuickReply> getQuickReplies() {
        return quickReplies;
    }

    public FacebookMessageNode setQuickReplies(final List<FacebookQuickReply> quickReplies) {
        this.quickReplies = quickReplies;
        return this;
    }

    public FacebookRichContentAttachment getAttachment() {
        return attachment;
    }

    public FacebookMessageNode setAttachment(final FacebookRichContentAttachment attachment) {
        this.attachment = attachment;
        return this;
    }
}