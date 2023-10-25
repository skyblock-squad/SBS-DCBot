package skyblocksquad.dcbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class Listeners extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setImage(event.getUser().getAvatarUrl())
                .setFooter("We're happy to meet you!")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getWelcomeChannel());
        if (textChannel != null)
            textChannel.sendMessage("Welcome " + event.getUser().getAsMention() + " to the Skyblock Squad Discord server").setEmbeds(embed).queue();
        Role memberRole = event.getGuild().getRolesByName(Main.getMemberRoleName(), true).get(0);
        event.getGuild().addRoleToMember(event.getMember(), memberRole).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have the permission").setEphemeral(true).queue();
            return;
        }

        if (event.getComponentId().equals("pingroles-news")) {
            Role dcNewsRole = event.getGuild().getRolesByName(Main.getPingRolesNewsRoleName(), true).get(0);
            if (!event.getMember().getRoles().contains(dcNewsRole)) {
                event.getGuild().addRoleToMember(event.getMember(), dcNewsRole).queue();
                event.reply("You will now receive update pings").setEphemeral(true).queue();
            } else {
                event.getGuild().removeRoleFromMember(event.getMember(), dcNewsRole).queue();
                event.reply("You will no longer receive update pings").setEphemeral(true).queue();
            }
        }
    }

}
