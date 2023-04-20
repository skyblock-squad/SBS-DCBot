package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
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
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final Map<Long, Instant> lastExecuted = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("applybetatester")) {
            long userId = event.getUser().getIdLong();
            Instant lastExecutedTime = getLastExecutedTime(userId);
            Instant currentTime = Instant.now();
            Duration duration = Duration.between(lastExecutedTime, currentTime);

            String dcUsername = event.getUser().getAsTag();
            String dcUserId = event.getUser().getId();
            String mcUsername = event.getOption("mcusername").getAsString();
            String reason = event.getOption("reason") == null ? "none given" : event.getOption("reason").getAsString();

            if(duration.toHours() < 24) {
                event.reply("You can only execute this command once a day to prevent spam.").setEphemeral(true).queue();
            } else {

                for (Role roles : event.getMember().getRoles()) {
                    if(roles.getName().equalsIgnoreCase("Beta Tester")) {
                        event.reply("You are already a beta tester.").setEphemeral(true).queue();
                        return;
                    }
                }

                updateLastExecutedTime(userId, currentTime);

                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Beta Tester Application")
                        .setColor(Color.BLUE)
                        .addField("Discord Username", dcUsername, false)
                        .addField("Minecraft Username", mcUsername, false)
                        .addField("Created", event.getUser().getTimeCreated().format(formatter), true)
                        .addField("Reason", reason, false)
                        .setFooter(dcUserId)
                        .build();

                event.getJDA().getTextChannelById(Main.getApplicationChannel())
                        .sendMessageEmbeds(embed)
                        .setActionRow(
                                Button.success("accept", "Accept"),
                                Button.danger("reject", "Reject")
                        )
                        .setSuppressedNotifications(true)
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
