package skyblocksquad.dcbot.util;

public record CachedMessage(long messageId, long authorId, boolean isBot, String contentRaw) {

    public String getAuthorAsMention() {
        return "<@" + Long.toUnsignedString(authorId) + ">";
    }

}