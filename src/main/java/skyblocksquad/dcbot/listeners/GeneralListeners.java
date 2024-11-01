package skyblocksquad.dcbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import skyblocksquad.dcbot.Main;

import java.awt.*;

public class GeneralListeners {

    private final Guild guild;
    private final @Nullable TextChannel welcomeChannel;

    public GeneralListeners(Guild guild) {
        this.guild = guild;
        welcomeChannel = Main.getConfig().getWelcomeChannelId() == 0 ? null :
                guild.getTextChannelById(Main.getConfig().getWelcomeChannelId());
    }

    @SubscribeEvent
    public void welcomeMember(GuildMemberJoinEvent event) {
        if (welcomeChannel == null || !event.getGuild().equals(guild)) return;

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setImage(event.getUser().getAvatarUrl())
                .setFooter("We're happy to meet you!")
                .build();

        TextChannel textChannel = event.getJDA().getTextChannelById(Main.getConfig().getWelcomeChannelId());
        if (textChannel != null)
            textChannel.sendMessage("Welcome " + event.getUser().getAsMention() + " to the Skyblock Squad Discord server").setEmbeds(embed).queue();
        Role memberRole = event.getGuild().getRoleById(Main.getConfig().getMemberRoleId());
        event.getGuild().addRoleToMember(event.getMember(), memberRole).queue();
    }

    @SubscribeEvent
    public void handlePingRolesButton(ButtonInteractionEvent event) {
        if (!event.isFromGuild() || !guild.equals(event.getGuild())) return;
        if (event.getMember() == null) return;

        if (event.getComponentId().equals("pingroles-news")) {
            Role dcNewsRole = event.getGuild().getRoleById(Main.getConfig().getPingRolesNewsPingRoleId());
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