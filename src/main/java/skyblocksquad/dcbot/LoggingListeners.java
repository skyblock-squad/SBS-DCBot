package skyblocksquad.dcbot;

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
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
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
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;
import skyblocksquad.dcbot.util.CachedMessage;
import skyblocksquad.dcbot.util.ColorUtils;
import skyblocksquad.dcbot.util.MessageCache;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class LoggingListeners extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        if (event.getAuthor().isBot()) return;

        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), false, event.getMessage().getContentRaw()));
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        if (event.getAuthor().isBot()) return;

        CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Message Edited")
                .setColor(Color.BLUE)
                .addField("User", event.getAuthor().getAsMention() + "(" + event.getAuthor().getName() + ")", false)
                .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false)
                .addField("Old Message", cachedMessage == null ? "not cached" : cachedMessage.getContentRaw(), false)
                .addField("New Message", event.getMessage().getContentRaw(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
        MessageCache.removeMessage(event.getMessageIdLong());
        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), event.getAuthor().isBot(), event.getMessage().getContentRaw()));
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (event.getChannel().equals(Main.getJDA().getTextChannelById(Main.getLogsChannel()))) return;
        final CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());
        MessageCache.removeMessage(event.getMessageIdLong());
        if (cachedMessage == null || cachedMessage.isBot()) return;

        final Consumer<User> action = (user) -> {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Message Deleted")
                    .setColor(Color.RED)
                    .addField("User", user == null ? (cachedMessage.getAuthorAsMention() + "(not cached)") : (user.getAsMention() + "(" + user.getName() + ")"), false)
                    .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false)
                    .addField("Message", cachedMessage.getContentRaw(), false)
                    .setFooter("")
                    .build();

            TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
            if (textChannel != null)
                textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        };

        Main.getJDA().retrieveUserById(cachedMessage.getAuthorId()).queue(action, (throwable -> action.accept(null)));
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ChannelType type = event.getChannel().getType();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setFooter("");

        switch (type) {
            case CATEGORY -> {
                embedBuilder.setTitle("Category Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
            }
            case GUILD_PUBLIC_THREAD -> {
                embedBuilder.setTitle("Thread Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Public", false);
            }
            case GUILD_PRIVATE_THREAD -> {
                embedBuilder.setTitle("Thread Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Private", false);
            }
            case TEXT -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Text Channel", false);
            }
            case VOICE -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Voice Channel", false);
            }
            case NEWS -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "News Channel", false);
            }
            case FORUM -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Forum Channel", false);
            }
            case STAGE -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Stage Channel", false);
            }
            default -> {
                embedBuilder.setTitle("Channel Created");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "unknown", false);
            }
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelType type = event.getChannel().getType();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .setFooter("");

        switch (type) {
            case CATEGORY -> {
                embedBuilder.setTitle("Category Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
            }
            case GUILD_PUBLIC_THREAD -> {
                embedBuilder.setTitle("Thread Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Public", false);
            }
            case GUILD_PRIVATE_THREAD -> {
                embedBuilder.setTitle("Thread Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Private", false);
            }
            case TEXT -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Text Channel", false);
            }
            case VOICE -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Voice Channel", false);
            }
            case NEWS -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "News Channel", false);
            }
            case FORUM -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Forum Channel", false);
            }
            case STAGE -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "Stage Channel", false);
            }
            default -> {
                embedBuilder.setTitle("Channel Deleted");
                embedBuilder.addField("Name", event.getChannel().getName(), false);
                embedBuilder.addField("Type", "unknown", false);
            }
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Joined")
                .setColor(Color.GREEN)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .addField("Created", event.getUser().getTimeCreated().format(formatter), true)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Name Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Old Name", event.getOldName(), false)
                .addField("New Name", event.getNewName(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Nick Change")
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Old Nick", event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName() + "(Username)", false)
                .addField("New Nick", event.getNewNickname() != null ? event.getNewNickname() : event.getUser().getName() + "(Username)", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        String oldDiscriminator = event.getOldValue();
        String newDiscriminator = event.getNewValue();
        if (oldDiscriminator.equals(newDiscriminator)) return;

        if (newDiscriminator.equals("0000")) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Member Updated To New Username")
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setColor(Color.BLUE)
                    .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                    .addField("Old Discriminator", event.getOldDiscriminator(), false)
                    .setFooter("")
                    .build();

            TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
            }
            return;
        }

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Discriminator Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Old Discriminator", event.getOldDiscriminator(), false)
                .addField("New Discriminator", event.getNewDiscriminator(), false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }


    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Avatar Change")
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .setFooter("")
                .build();

        FileUpload oldImage = null;
        if (event.getOldAvatar() != null) {
            oldImage = downloadAvatar("oldImage.png", event.getOldAvatar());
        }

        FileUpload newImage = null;
        if (event.getNewAvatar() != null) {
            newImage = downloadAvatar("newImage.png", event.getNewAvatar());
        }

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            MessageCreateAction messageAction = textChannel.sendMessageEmbeds(embed);
            if (oldImage != null) messageAction.addFiles(oldImage);
            if (newImage != null) messageAction.addFiles(newImage);
            messageAction.setSuppressedNotifications(Main.getLogsSilent()).queue();

        }
    }


    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Left")
                .setColor(Color.RED)
                .addField("User", "<@" + event.getUser().getId() + ">" + " (" + event.getUser().getName() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        StringBuilder rolesBuilder = new StringBuilder();
        for (Role role : event.getRoles()) {
            rolesBuilder.append("- ").append(role.getAsMention()).append(" (").append(role.getName()).append(")").append("\n");
        }
        String rolesList = rolesBuilder.toString();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Role Update")
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Roles added", rolesList, false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        StringBuilder rolesBuilder = new StringBuilder();
        for (Role role : event.getRoles()) {
            rolesBuilder.append("- ").append(role.getAsMention()).append(" (").append(role.getName()).append(")").append("\n");
        }
        String rolesList = rolesBuilder.toString();

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Role Update")
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Roles removed", rolesList, false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
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
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
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
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
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
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
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
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
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
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
        String inviteExpiration = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis() + (event.getInvite().getMaxAge() * 1000L));

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Invite Created")
                .setColor(Color.GREEN)
                .addField("User", event.getInvite().getInviter() == null ? "Could not get user" : event.getInvite().getInviter().getAsMention() + " (" + event.getInvite().getInviter().getName() + ")", false)
                .addField("Invite", event.getInvite().getUrl(), false)
                .addField("Channel", event.getInvite().getChannel() != null ? event.getInvite().getChannel().getName() : "Could not get channel", false)
                .addField("Expiration", event.getInvite().getMaxAge() != 0 ? inviteExpiration : "never", false)
                .addField("Max Uses", event.getInvite().getMaxUses() != 0 ? String.valueOf(event.getInvite().getMaxUses()) : "∞", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false);

        if (event.getChannelLeft() != null && event.getChannelJoined() != null) {
            embedBuilder.setTitle("Voice: User Moved");
            embedBuilder.setColor(Color.BLUE);
            embedBuilder.addField("From Channel", event.getChannelLeft().getAsMention() + " (" + event.getChannelLeft().getName() + ")", false);
            embedBuilder.addField("To Channel", event.getChannelJoined().getAsMention() + " (" + event.getChannelJoined().getName() + ")", false);
        } else if (event.getChannelLeft() != null) {
            embedBuilder.setTitle("Voice: User Left");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField("Channel", event.getChannelLeft().getAsMention() + " (" + event.getChannelLeft().getName() + ")", false);
        } else if (event.getChannelJoined() != null) {
            embedBuilder.setTitle("Voice: User Joined");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.addField("Channel", event.getChannelJoined().getAsMention() + " (" + event.getChannelJoined().getName() + ")", false);
        } else return;

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getVoiceLogsChannel());
        if (textChannel != null)
            textChannel.sendMessageEmbeds(embedBuilder.build()).setSuppressedNotifications(Main.getLogsSilent()).queue();
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildMuted() ? "muted" : "unmuted"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getVoiceLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    @Override
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildDeafened() ? "deafened" : "undeafened"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .setFooter("")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getVoiceLogsChannel());
        if (textChannel != null) {
            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
        }
    }

    private FileUpload downloadAvatar(String fileName, ImageProxy imageProxy) {
        return FileUpload.fromStreamSupplier(fileName, () -> {
            try {
                return imageProxy.download().get();
            } catch (Exception e) {
                return null;
            }
        });
    }
}
