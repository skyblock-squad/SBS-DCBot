package de.skyblocksquad.dcbot.config;

public record CachedMessage(long messageId, long authorId, boolean isBot, String contentRaw) {

    public String authorAsMention() {
        return "<@" + Long.toUnsignedString(authorId) + ">";
    }

}