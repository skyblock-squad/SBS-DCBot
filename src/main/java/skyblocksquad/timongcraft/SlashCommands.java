package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


public class SlashCommands extends ListenerAdapter {
    private static final String applicationsChannel = "1092089903690559529";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
    private final Map<Long, Instant> lastExecuted = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("applybetatester")) {
            User user = event.getUser();
            long userId = user.getIdLong();
            Instant lastExecutedTime = getLastExecutedTime(userId);
            Instant currentTime = Instant.now();
            Duration duration = Duration.between(lastExecutedTime, currentTime);

            String dcUsername = event.getUser().getAsTag();
            String dcUserId = event.getUser().getId();
            String mcUsername = event.getOption("mcusername").getAsString();
            String reason = event.getOption("reason").getAsString();

            if (duration.toHours() < 24) {
                event.reply("You can only execute this command once a day to prevent spam.").setEphemeral(true).queue();
            } else {
                updateLastExecutedTime(userId, currentTime);

                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Beta Tester Application")
                        .setColor(Color.BLUE)
                        .addField("Discord Username", dcUsername, false)
                        .addField("Created", event.getUser().getTimeCreated().format(formatter), true)
                        .addField("Minecraft Username", mcUsername, false)
                        .addField("Reason", reason, false)
                        .setFooter(dcUserId)
                        .build();

                event.getJDA().getTextChannelById(applicationsChannel)
                        .sendMessageEmbeds(embed)
                        .setActionRow(
                                Button.success("accept", "Accept"),
                                Button.danger("reject", "Reject")
                        )
                        .queue(message -> {
                            event.reply("Your application has been submitted.").setEphemeral(true).queue();
                        });
            }

        }
    }

    private Instant getLastExecutedTime(long userId) {
        return lastExecuted.getOrDefault(userId, Instant.EPOCH);
    }

    private void updateLastExecutedTime(long userId, Instant lastExecutedTime) {
        lastExecuted.put(userId, lastExecutedTime);
    }

}
