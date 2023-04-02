package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;


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
                            .setTitle("Beta Tester Application")
                            .setColor(Color.GREEN)
                            .addField("Where?", "Skyblock Squad", false)
                            .addField("Status", "You have been accepted as a beta tester", false)
                            .setFooter("accepted by " + event.getUser().getAsTag());

                    privateChannel.sendMessageEmbeds(embed.build()).queue();
                });
                    }, failure -> {
                event.reply("Couldn't get user").setEphemeral(true);
            });

            event.getMessage().delete().queue();
        } else if (event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
