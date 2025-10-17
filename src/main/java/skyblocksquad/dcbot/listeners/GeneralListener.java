package skyblocksquad.dcbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import skyblocksquad.dcbot.Main;

import java.awt.*;

public class GeneralListener {

    private final long guildId;
    private final long memberRoleId;
    private final long pingRolesNewsPingRoleId;
    private final long welcomeChannelId;

    public GeneralListener(long guildId) {
        this.guildId = guildId;
        this.memberRoleId = Main.getConfig().getMemberRoleId();
        this.pingRolesNewsPingRoleId = Main.getConfig().getPingRolesNewsPingRoleId();
        this.welcomeChannelId = Main.getConfig().getWelcomeChannelId();
    }

    @SubscribeEvent
    public void welcomeMember(GuildMemberJoinEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setImage(event.getUser().getAvatarUrl())
                .setFooter("We're happy to meet you!")
                .build();

        TextChannel textChannel = event.getJDA().getTextChannelById(welcomeChannelId);
        if (textChannel != null)
            textChannel.sendMessage("Welcome " + event.getUser().getAsMention() + " to the Skyblock Squad Discord server").setEmbeds(embed).queue();
        Role memberRole = event.getGuild().getRoleById(memberRoleId);
        event.getGuild().addRoleToMember(event.getMember(), memberRole).queue();
    }

    @SubscribeEvent
    public void handlePingRolesButton(ButtonInteractionEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId
                || event.getMember() == null) return;

        if (event.getComponentId().equals("pingroles-news")) {
            Role dcNewsRole = event.getGuild().getRoleById(pingRolesNewsPingRoleId);
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