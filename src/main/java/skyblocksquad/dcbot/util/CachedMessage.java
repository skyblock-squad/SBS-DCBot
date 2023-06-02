package skyblocksquad.dcbot.util;

public class CachedMessage {
    private long messageId;
    private long authorId;
    private boolean isBot;
    private String contentRaw;

    public CachedMessage(long messageId, long authorId, boolean isBot, String contentRaw) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.isBot = isBot;
        this.contentRaw = contentRaw;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getAuthorAsMention() {
        return "<@" + Long.toUnsignedString(authorId) + ">";
    }

    public String getContentRaw() {
        return contentRaw;
    }

    public boolean isBot() {
        return isBot;
    }

}
