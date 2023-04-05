package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
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
            String dcUsername = null;
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

            for (MessageEmbed.Field field : fields) {
                String name = field.getName();
                String value = field.getValue();

                if (name.equals("Discord Username")) {
                    dcUsername = value;
                    break;
                }
            }

            Member member = event.getGuild().getMemberByTag(dcUsername);
            event.getGuild().addRoleToMember(member, betaTesterRole).queue();
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

            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Accepted Beta Tester")
                    .setColor(Color.BLUE)
                    .addField("Discord Username", dcUsername, false)
                    .addField("Minecraft Username", mcUsername, false)
                    .setFooter("accepted by " + event.getUser().getAsTag())
                    .build();

            event.getMessage().delete().queue();
            event.getChannel()
                    .sendMessageEmbeds(embed)
                    .setSuppressedNotifications(true)
                    .queue();
        } else if (event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
