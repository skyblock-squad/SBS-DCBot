package de.skyblocksquad.dcbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class EmbedUtils {

    public static void addSplitField(EmbedBuilder eb, String title, String content, boolean inline) {
        int totalParts = (content.length() + MessageEmbed.VALUE_MAX_LENGTH - 1) / MessageEmbed.VALUE_MAX_LENGTH;

        for (int i = 0; i < totalParts; i++) {
            int start = i * MessageEmbed.VALUE_MAX_LENGTH;
            int end = Math.min(content.length(), start + MessageEmbed.VALUE_MAX_LENGTH);

            String partTitle = totalParts > 1 ? title + " (" + (i + 1) + "/" + totalParts + ")" : title;

            eb.addField(partTitle, content.substring(start, end), inline);
        }
    }

    private EmbedUtils() {}

}