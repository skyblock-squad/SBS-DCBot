package skyblocksquad.dcbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
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
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.jetbrains.annotations.Nullable;
import skyblocksquad.dcbot.Main;
import skyblocksquad.dcbot.config.CachedMessage;
import skyblocksquad.dcbot.config.MessageCache;
import skyblocksquad.dcbot.util.ColorUtils;
import skyblocksquad.dcbot.util.EmbedUtils;
import skyblocksquad.dcbot.util.TimeUtils;

import java.awt.*;
import java.text.SimpleDateFormat;

public class LoggingListener {

    private static @Nullable MessageCreateAction buildLogMessage(@Nullable TextChannel channel, MessageEmbed embed, MessageEmbed... embeds) {
        if (channel == null) return null;

        return channel.sendMessageEmbeds(embed, embeds)
                .setSuppressedNotifications(Main.getConfig().isSilentLogMessages());
    }

    private static @Nullable FileUpload downloadAvatar(String fileName, @Nullable ImageProxy imageProxy) {
        if (imageProxy == null) return null;

        try {
            return FileUpload.fromData(imageProxy.download().get(), fileName);
        } catch (Exception e) {
            return null;
        }
    }

    private final long guildId;
    private final @Nullable TextChannel logChannel;
    private final @Nullable TextChannel voiceLogChannel;

    public LoggingListener(Guild guild) {
        this.guildId = guild.getIdLong();
        logChannel = Main.getConfig().getLogsChannelId() == 0 ? null :
                guild.getTextChannelById(Main.getConfig().getLogsChannelId());
        voiceLogChannel = Main.getConfig().getVoiceLogsChannelId() == 0 ? null :
                guild.getTextChannelById(Main.getConfig().getVoiceLogsChannelId());
    }

    @SubscribeEvent
    public void storeIncomingMessage(MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        if (event.getChannel().equals(logChannel)) return;
        if (event.getAuthor().isBot()) return;

        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), false, event.getMessage().getContentRaw()));
    }

    @SubscribeEvent
    public void logMessageEdit(MessageUpdateEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        if (event.getChannel().equals(logChannel)) return;
        if (event.getAuthor().isBot()) return;

        CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Message Edited")
                .setColor(Color.BLUE)
                .addField("User", event.getAuthor().getAsMention() + "(" + event.getAuthor().getName() + ")", false)
                .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false);
        EmbedUtils.addSplitField(embedBuilder, "Old Message", cachedMessage == null ? "not cached" : cachedMessage.contentRaw(), false);
        EmbedUtils.addSplitField(embedBuilder, "New Message (" + event.getMessage().getJumpUrl() + ")", event.getMessage().getContentRaw(), false);

        sendLogMessage(embedBuilder.build());
        MessageCache.removeMessage(event.getMessageIdLong());
        MessageCache.addMessage(new CachedMessage(event.getMessageIdLong(), event.getAuthor().getIdLong(), event.getAuthor().isBot(), event.getMessage().getContentRaw()));
    }

    @SubscribeEvent
    public void logMessageDeletion(MessageDeleteEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        if (event.getChannel().equals(logChannel)) return;
        final CachedMessage cachedMessage = MessageCache.getMessage(event.getMessageIdLong());
        MessageCache.removeMessage(event.getMessageIdLong());
        if (cachedMessage == null || cachedMessage.isBot()) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Message Deleted")
                .setColor(Color.RED)
                .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false);
        EmbedUtils.addSplitField(embedBuilder, "Message (" + TimeUtil.getTimeCreated(cachedMessage.messageId()).format(TimeUtils.DD_MM_YYY_HH_MM_SS) + ")", cachedMessage.contentRaw(), false);

        event.getJDA().retrieveUserById(cachedMessage.authorId()).queue(
                user -> {
                    embedBuilder.getFields().add(0,
                            new MessageEmbed.Field("User", user.getAsMention() + " (" + user.getName() + ")", false));
                    sendLogMessage(embedBuilder.build());
                },
                throwable -> {
                    embedBuilder.getFields().add(0,
                            new MessageEmbed.Field("User", cachedMessage.authorAsMention() + " (not cached)", false));
                    sendLogMessage(embedBuilder.build());
                }
        );
    }

    @SubscribeEvent
    public void logChannelCreation(ChannelCreateEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.GREEN);

        buildChannelUpdateEmbed(event.getChannel(), embedBuilder, true);

        sendLogMessage(embedBuilder.build());
    }

    @SubscribeEvent
    public void logChannelUpdate(GenericChannelUpdateEvent<?> event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        //suppress voice channel status updates
        if (event.getChannel().getType().equals(ChannelType.VOICE)
                && event.getPropertyIdentifier().equals("status")) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.BLUE);

        buildChannelUpdateEmbed(event.getChannel(), embedBuilder, null);

        embedBuilder.addField("Updated", event.getPropertyIdentifier(), false);

        sendLogMessage(embedBuilder.build());
    }

    @SubscribeEvent
    public void logChannelDeletion(ChannelDeleteEvent event) {
        if (!event.isFromGuild() || event.getGuild().getIdLong() != guildId) return;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED);

        buildChannelUpdateEmbed(event.getChannel(), embedBuilder, false);

        sendLogMessage(embedBuilder.build());
    }

    @SubscribeEvent
    public void logMemberJoin(GuildMemberJoinEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Joined")
                .setColor(Color.GREEN)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .addField("Created", event.getUser().getTimeCreated().format(TimeUtils.DD_MM_YYY_HH_MM_SS), true)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logMemberUsernameChange(UserUpdateNameEvent event) {
        if (isNotInGuild(event.getUser())) return;

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Name Change")
                .setThumbnail(event.getUser().getAvatarUrl())
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Old Name", event.getOldName(), false)
                .addField("New Name", event.getNewName(), false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logMemberNicknameChange(GuildMemberUpdateNicknameEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Nick Change")
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .addField("Old Nick", event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName() + "(Username)", false)
                .addField("New Nick", event.getNewNickname() != null ? event.getNewNickname() : event.getUser().getName() + "(Username)", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    @Deprecated(forRemoval = true)
    public void logMemberDiscriminatorChange(UserUpdateDiscriminatorEvent event) {
        if (isNotInGuild(event.getUser())) return;
        String oldDiscriminator = event.getOldValue();
        String newDiscriminator = event.getNewValue();
        if (oldDiscriminator.equals(newDiscriminator)) return;

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (newDiscriminator.equals("0000")) {
            embedBuilder
                    .setTitle("Member Updated To New Username")
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setColor(Color.BLUE)
                    .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                    .addField("Old Discriminator", event.getOldDiscriminator(), false);
        } else {
            embedBuilder
                    .setTitle("Member Discriminator Change")
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setColor(Color.BLUE)
                    .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                    .addField("Old Discriminator", event.getOldDiscriminator(), false)
                    .addField("New Discriminator", event.getNewDiscriminator(), false);
        }

        sendLogMessage(embedBuilder.build());
    }

    @SubscribeEvent
    public void logMemberAvatarChange(UserUpdateAvatarEvent event) {
        if (isNotInGuild(event.getUser())) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Avatar Change")
                .setColor(Color.BLUE)
                .addField("User", event.getUser().getAsMention() + " (" + event.getUser().getName() + ")", false)
                .build();

        FileUpload oldImage = null;
        if (event.getOldAvatar() != null)
            oldImage = downloadAvatar("oldImage.png", event.getOldAvatar());

        FileUpload newImage = null;
        if (event.getNewAvatar() != null)
            newImage = downloadAvatar("newImage.png", event.getNewAvatar());

        MessageCreateAction logMessageAction = buildLogMessage(logChannel, embed);
        if (logMessageAction != null) {
            if (oldImage != null) {
                logMessageAction.addFiles(oldImage);
            }
            if (newImage != null) {
                logMessageAction.addFiles(newImage);
            }
            logMessageAction.queue();
        }
    }

    @SubscribeEvent
    public void logMemberLeave(GuildMemberRemoveEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Member Left")
                .setColor(Color.RED)
                .addField("User", "<@" + event.getUser().getId() + ">" + " (" + event.getUser().getName() + ")", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logMemberRoleAddition(GuildMemberRoleAddEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
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
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logMemberRoleRemoval(GuildMemberRoleRemoveEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
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
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logRoleCreation(RoleCreateEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Created")
                .setColor(Color.GREEN)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logRoleDeletion(RoleDeleteEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Deleted")
                .setColor(Color.RED)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logRoleNameChange(RoleUpdateNameEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Updated")
                .setColor(Color.BLUE)
                .addField("Role", event.getRole().getAsMention(), false)
                .addField("Old Name", event.getOldValue(), false)
                .addField("New Name", event.getNewValue(), false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logRoleColorChange(RoleUpdateColorEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        String oldColorName = event.getOldColor() == null ? "unknown" : ColorUtils.getColorNameFromColor(event.getOldColor());
        String newColorName = event.getNewColor() == null ? "unknown" : ColorUtils.getColorNameFromColor(event.getNewColor());

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Role Updated")
                .setColor(Color.BLUE)
                .addField("Role", event.getRole().getAsMention() + " (" + event.getRole().getName() + ")", false)
                .addField("Old Color", oldColorName + " (" + ColorUtils.rgbToHexString(event.getOldColorRaw()) + ")", false)
                .addField("New Color", newColorName + " (" + ColorUtils.rgbToHexString(event.getNewColorRaw()) + ")", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logRolePermissionsChange(RoleUpdatePermissionsEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
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
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logInviteCreation(GuildInviteCreateEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        String inviteExpiration = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(System.currentTimeMillis() + (event.getInvite().getMaxAge() * 1000L));

        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Invite Created")
                .setColor(Color.GREEN)
                .addField("User", event.getInvite().getInviter() == null ? "Could not get user" : event.getInvite().getInviter().getAsMention() + " (" + event.getInvite().getInviter().getName() + ")", false)
                .addField("Invite", event.getInvite().getUrl(), false)
                .addField("Channel", event.getInvite().getChannel() != null ? event.getInvite().getChannel().getName() : "Could not get channel", false)
                .addField("Expiration", event.getInvite().getMaxAge() != 0 ? inviteExpiration : "never", false)
                .addField("Max Uses", event.getInvite().getMaxUses() != 0 ? String.valueOf(event.getInvite().getMaxUses()) : "∞", false)
                .build();

        sendLogMessage(embed);
    }

    @SubscribeEvent
    public void logMemberVoiceStateChange(GuildVoiceUpdateEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
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

        MessageCreateAction logMessageAction = buildLogMessage(voiceLogChannel, embedBuilder.build());
        if (logMessageAction != null)
            logMessageAction.queue();
    }

    @SubscribeEvent
    public void logMemberVoiceMuteStateChange(GuildVoiceGuildMuteEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildMuted() ? "muted" : "unmuted"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .build();

        MessageCreateAction logMessageAction = buildLogMessage(voiceLogChannel, embed);
        if (logMessageAction != null)
            logMessageAction.queue();
    }

    @SubscribeEvent
    public void logMemberVoiceDeafeningStateChange(GuildVoiceGuildDeafenEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice: User " + (event.isGuildDeafened() ? "deafened" : "undeafened"))
                .setColor(Color.BLUE)
                .addField("User", event.getMember().getAsMention() + " (" + event.getMember().getUser().getName() + ")", false)
                .build();

        MessageCreateAction logMessageAction = buildLogMessage(voiceLogChannel, embed);
        if (logMessageAction != null)
            logMessageAction.queue();
    }

    private void sendLogMessage(MessageEmbed embed, MessageEmbed... embeds) {
        MessageCreateAction logMessage = buildLogMessage(logChannel, embed, embeds);
        if (logMessage == null) return;

        logMessage.queue();
    }

    private EmbedBuilder buildChannelUpdateEmbed(ChannelUnion channel, EmbedBuilder embedBuilder, @Nullable Boolean state) {
        embedBuilder.addField("Name", channel.getName(), false);
        switch (channel.getType()) {
            case CATEGORY -> embedBuilder.setTitle("Category " + (state != null ? state ? "Created" : "Removed" : "Updated"));
            case GUILD_PUBLIC_THREAD -> embedBuilder.setTitle("Thread " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Public", false);
            case GUILD_PRIVATE_THREAD -> embedBuilder.setTitle("Thread " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Private", false);
            case TEXT -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Text Channel", false);
            case VOICE -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Voice Channel", false);
            case NEWS -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "News Channel", false);
            case FORUM -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Forum Channel", false);
            case STAGE -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "Stage Channel", false);
            default -> embedBuilder.setTitle("Channel " + (state != null ? state ? "Created" : "Removed" : "Updated"))
                    .addField("Type", "unknown", false);
        }

        return embedBuilder;
    }

    private boolean isNotInGuild(User user) {
        for (Guild mutualGuild : user.getMutualGuilds()) {
            if (mutualGuild.getIdLong() != guildId) continue;

            return false;
        }

        return true;
    }

}