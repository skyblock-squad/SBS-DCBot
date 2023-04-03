package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;


public class Listeners extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have the permission").setEphemeral(true).queue();
            return;
        }

        if (event.getComponentId().equals("accept")) {
            Role betaTesterRole = event.getGuild().getRolesByName("Beta Tester", true).get(0);
            event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getUser().getId()), betaTesterRole).queue();
            String appliedUserId = event.getMessage().getEmbeds().get(0).getFooter().getText();

            event.getJDA().retrieveUserById(appliedUserId).queue(user -> {
                        user.openPrivateChannel().queue(privateChannel -> {
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
                    }, failure -> {
                event.reply("Couldn't get user").setEphemeral(true);
            });
            String dcUsername = event.getUser().getAsTag();
            String mcUsername = null;

            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            List<MessageEmbed.Field> fields = embed.getFields();
            for (MessageEmbed.Field field : fields) {
                String name = field.getName();
                String value = field.getValue();

                if (name.equals("Minecraft Username")) {
                    mcUsername = value;
                    break;
                }
            }

            MessageEmbed embed2 = new EmbedBuilder()
                    .setTitle("Accepted Beta Tester")
                    .setColor(Color.BLUE)
                    .addField("Discord Username", dcUsername, false)
                    .addField("Minecraft Username", mcUsername, false)
                    .setFooter("accepted by " + event.getUser().getAsTag())
                    .build();

            event.getMessage().delete().queue();
            event.getChannel()
                    .sendMessageEmbeds(embed2)
                    .setSuppressedNotifications(true)
                    .queue();
        } else if (event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
