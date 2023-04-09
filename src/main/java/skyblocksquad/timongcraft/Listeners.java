package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;


public class Listeners extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have the permission").setEphemeral(true).queue();
            return;
        }

        if (event.getComponentId().equals("accept")) {
            Role betaTesterRole = event.getGuild().getRolesByName("Beta Tester", true).get(0);
            String mcUsername = null;
            MessageEmbed readembed = event.getMessage().getEmbeds().get(0);
            List<MessageEmbed.Field> fields = readembed.getFields();

            for (MessageEmbed.Field field : fields) {
                String name = field.getName();
                String value = field.getValue();

                if (name.equals("Minecraft Username")) {
                    mcUsername = value;
                    break;
                }
            }

            final String finMcUsername = mcUsername;
            String appliedUserId = event.getMessage().getEmbeds().get(0).getFooter().getText();
            event.getGuild().retrieveMemberById(appliedUserId).queue(member -> {
                event.getGuild().addRoleToMember(member, betaTesterRole).queue();

                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Accepted as Beta Tester")
                            .setColor(Color.GREEN)
                            .addField("DC-Server", "Skyblock Squad", false)
                            .addField("Info", "You can now have access to <#1086705968009719899> and <#1086707130695954472>", false)
                            .setFooter("accepted by " + event.getUser().getAsTag());

                    privateChannel
                            .sendMessageEmbeds(embed.build())
                            .queue();
                });

                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Accepted Beta Tester")
                        .setColor(Color.BLUE)
                        .addField("Discord Username", member.getUser().getAsTag(), false)
                        .addField("Minecraft Username", finMcUsername, false)
                        .setFooter("accepted by " + event.getUser().getAsTag())
                        .build();

                //event.getMessage().editMessage(MessageEditData.fromEmbeds(embed)).setActionRow().queue();
                event.getMessage().delete().queue();
                event.getChannel()
                        .sendMessageEmbeds(embed)
                        .setSuppressedNotifications(true)
                        .queue();
                try {
                    Files.write(Path.of("users.log"), (member.getId() + " " + finMcUsername + "\n").getBytes(), StandardOpenOption.APPEND);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, failure -> {
                event.reply("Couldn't get user").setEphemeral(true);
            });



        } else if (event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
