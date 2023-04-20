package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import skyblocksquad.timongcraft.util.CachedMessage;
import skyblocksquad.timongcraft.util.ColorUtils;
import skyblocksquad.timongcraft.util.MessageCache;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

public class LoggingListeners extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        if(event.getAuthor().isBot()) return;

        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), false, event.getMessage().getContentRaw()));
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if(event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        if(event.getAuthor().isBot()) return;

        CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Message Edited")
                .setColor(Color.BLUE)
                .addField("User", event.getAuthor().getAsTag(), false)
                .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false)
                .addField("Old Message", cachedMessage == null ? "not cached" : cachedMessage.getContentRaw(), false)
                .addField("New Message", event.getMessage().getContentRaw(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        MessageCache.removeMessage(event.getMessageIdLong());
        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), event.getAuthor().isBot(), event.getMessage().getContentRaw()));
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if(event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        final CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());
        MessageCache.removeMessage(event.getMessageIdLong());

        if(cachedMessage == null || cachedMessage.isBot()) { return; }

        final Consumer<User> action = (user) -> {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Message Deleted")
                    .setColor(Color.RED)
                    .addField("User", user == null ? (cachedMessage.getAuthorAsMention() + "(not cached)") : (user.getAsMention() + "(" + user.getAsTag() + ")"), false)
                    .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false)
                    .addField("Message", cachedMessage.getContentRaw(), false)
                    .setFooter("")
                    .build();

            TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
            if(textChannel != null)
                textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        };

        Main.getJDA().retrieveUserById(cachedMessage.getAuthorId()).queue(action, (throwable -> action.accept(null)));
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ChannelType type = event.getChannel().getType();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GREEN)
                .addField("Channel Name", event.getChannel().getName(), false)
                .setFooter("");

        switch (type) {
            case CATEGORY -> {
                embedBuilder.setTitle("Category Created");
                embedBuilder.addField("Category Name", event.getChannel().getName(), false);
            }
            case TEXT -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Text Channel", false);
            }
            case VOICE -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Voice Channel", false);
            }
            case NEWS -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "News Channel", false);
            }
            case FORUM -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Forum Channel", false);
            }
            case STAGE -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Stage Channel", false);
            }
            default -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "unknown", false);
            }
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelType type = event.getChannel().getType();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .addField("Channel Name", event.getChannel().getName(), false)
                .setFooter("");

        switch (type) {
            case CATEGORY -> {
                embedBuilder.setTitle("Category Deleted");
                embedBuilder.addField("Category Name", event.getChannel().getName(), false);
            }
            case TEXT -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Text Channel", false);
            }
            case VOICE -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Voice Channel", false);
            }
            case NEWS -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "News Channel", false);
            }
            case FORUM -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Forum Channel", false);
            }
            case STAGE -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Stage Channel", false);
            }
            default -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Channel Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "unknown", false);
            }
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Joined")
                .setColor(Color.GREEN)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getAsTag() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Name Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .addField("Old Name", event.getOldName(), false)
                .addField("New Name", event.getNewName(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Nick Change")
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .addField("Old Nick", event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName() + "(Username)", false)
                .addField("New Nick", event.getNewNickname() != null ? event.getNewNickname() : event.getUser().getName() + "(Username)", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Discriminator Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .addField("Old Discriminator", event.getOldDiscriminator(), false)
                .addField("New Discriminator", event.getNewDiscriminator(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    /*
    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Avatar Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }
    */

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Left")
                .setColor(Color.RED)
                .addField("User", "<@" + event.getUser().getId() + ">" + " (" + event.getUser().getAsTag() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        StringBuilder rolesBuilder = new StringBuilder();
        for (Role role : event.getRoles()) {
            rolesBuilder.append("- ").append(role.getAsMention() + " (" + role.getName() + ")").append("\n");
        }
        String rolesList = rolesBuilder.toString();
        
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Role Update")
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .addField("Roles added", rolesList, false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        StringBuilder rolesBuilder = new StringBuilder();
        for (Role role : event.getRoles()) {
            rolesBuilder.append("- ").append(role.getAsMention() + " (" + role.getName() + ")").append("\n");
        }
        String rolesList = rolesBuilder.toString();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Role Update")
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getUser().getAsTag() + ")", false)
                .addField("Roles removed", rolesList, false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Created")
                .setColor(Color.GREEN)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Deleted")
                .setColor(Color.RED)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Updated")
                .setColor(Color.BLUE)
                .addField("Role", event.getRole().getAsMention(), false)
                .addField("Old Name", event.getOldValue(), false)
                .addField("New Name", event.getNewValue(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onRoleUpdateColor(RoleUpdateColorEvent event) {
        String oldColorName = event.getOldColor() == null ? "none" : ColorUtils.getColorNameFromColor(event.getOldColor());
        String newColorName = event.getNewColor() == null ? "none" : ColorUtils.getColorNameFromColor(event.getNewColor());

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Updated")
                .setColor(Color.BLUE)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .addField("Old Color", oldColorName + " (" + event.getOldValue() + ")", false)
                .addField("New Color", newColorName + " (" + event.getNewValue() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        StringBuilder permissionsBuilder = new StringBuilder();
        for (Permission permission : event.getRole().getPermissions()) {
            permissionsBuilder.append("- ").append(permission.getName()).append("\n");
        }
        String permissionsList = permissionsBuilder.toString();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Updated")
                .setColor(Color.BLUE)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .addField("Permissions", permissionsList, false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getAsTag() + ")", false);

        if(event.getChannelLeft() != null && event.getChannelJoined() != null) {
            embedBuilder.setTitle("Voice: User Moved");
            embedBuilder.setColor(Color.BLUE);
            embedBuilder.addField("From Channel", event.getChannelLeft().getAsMention() + " (" + event.getChannelLeft().getName() + ")", false);
            embedBuilder.addField("To Channel", event.getChannelJoined().getAsMention() + " (" + event.getChannelJoined().getName() + ")", false);
        } else if(event.getChannelLeft() != null) {
            embedBuilder.setTitle("Voice: User Left");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField("Channel", event.getChannelLeft().getAsMention() + " (" + event.getChannelLeft().getName() + ")", false);
        } else if(event.getChannelJoined() != null) {
            embedBuilder.setTitle("Voice: User Joined");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.addField("Channel", event.getChannelJoined().getAsMention() + " (" + event.getChannelJoined().getName() + ")", false);
        } else return;

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildMuted() ? "muted" : "unmuted"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getAsTag() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildDeafened() ? "deafened" : "undeafened"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getAsTag() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
        String inviteExpiration = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis() + (event.getInvite().getMaxAge() * 1000));

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Invite Created")
                .setColor(Color.GREEN)
                .addField("User", event.getInvite().getInviter().getAsMention() + " (" + event.getInvite().getInviter().getAsTag() + ")", false)
                .addField("Invite", event.getInvite().getUrl(), false)
                .addField("Expiration", event.getInvite().getMaxAge() != 0 ? inviteExpiration : "never", false)
                .addField("Max Uses", event.getInvite().getMaxUses() != 0 ? String.valueOf(event.getInvite().getMaxUses()) : "∞", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if(textChannel != null)
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }
}
