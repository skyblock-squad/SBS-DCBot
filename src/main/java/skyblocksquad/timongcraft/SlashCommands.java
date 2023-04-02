package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.format.DateTimeFormatter;


public class SlashCommands extends ListenerAdapter {
    private static final String channel = "949275936279375894";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("getbetatester")) {
            String dcUsername = event.getUser().getAsTag();
            String mcUsername = event.getOption("mcusername").getAsString();
            String reason = event.getOption("reason").getAsString();

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Beta Tester Application")
                    .setColor(Color.BLUE)
                    .addField("Discord Username", dcUsername, false)
                    .addField("Created", event.getUser().getTimeCreated().format(formatter), true)
                    .addField("Minecraft Username", mcUsername, false)
                    .addField("Reason", reason, false)
                    .build();

            event.getJDA().getTextChannelById(channel)
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have the permission").setEphemeral(true).queue();
            return;
        }

        if (event.getComponentId().equals("accept")) {
            Role betaTesterRole = event.getGuild().getRolesByName("Beta Tester", true).get(0);
            event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUser().getId()), betaTesterRole).queue();

            event.getUser().openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Beta Tester Application")
                        .setColor(Color.GREEN)
                        .addField("Status", "You have been accepted as a beta tester", false)
                        .setFooter("from Server: Skyblock Squad");

                privateChannel.sendMessageEmbeds(embed.build()).queue();
            });

            event.getMessage().delete().queue();
        } else if (event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
