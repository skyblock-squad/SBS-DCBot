package de.skyblocksquad.dcbot.listeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import de.skyblocksquad.dcbot.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChannelActivityManager {

    private static final int DEFAULT_SCHEDULED_UPDATE_DELAY = 10_000;

    private final long guildId;
    private final Set<VoiceChannel> scheduledUpdates = new CopyOnWriteArraySet<>();
    private final Set<VoiceChannel> randomChannels = new CopyOnWriteArraySet<>();

    public ChannelActivityManager(Guild guild) {
        this.guildId = guild.getIdLong();

        int delay = 0;
        for (VoiceChannel channel : guild.getVoiceChannels()) {
            scheduleStatusUpdateIfAutomated(channel, delay);
            delay += 5_000;
        }

        //randomize status
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int delay = 0;
                for (VoiceChannel channel : randomChannels) {
                    scheduleStatusUpdateIfAutomated(channel, delay);

                    delay += 30_000 / randomChannels.size();
                }
            }
        }, 0, 30_000);
    }

    @SubscribeEvent
    public void activityStart(UserActivityStartEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        if (event.getNewActivity().getType().equals(Activity.ActivityType.CUSTOM_STATUS)) return;
        GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null) return;
        VoiceChannel channel = voiceState.getChannel() == null ? null : voiceState.getChannel().getType() == ChannelType.VOICE ? voiceState.getChannel().asVoiceChannel() : null;
        if (channel == null) return;

        scheduleStatusUpdateIfAutomated(channel, DEFAULT_SCHEDULED_UPDATE_DELAY);
    }

    @SubscribeEvent
    public void activityEnd(UserActivityEndEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        if (event.getOldActivity().getType().equals(Activity.ActivityType.CUSTOM_STATUS)) return;
        GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null) return;
        VoiceChannel channel = voiceState.getChannel() == null ? null : voiceState.getChannel().getType() == ChannelType.VOICE ? voiceState.getChannel().asVoiceChannel() : null;
        if (channel == null) return;

        scheduleStatusUpdateIfAutomated(channel, DEFAULT_SCHEDULED_UPDATE_DELAY);
    }

    @SubscribeEvent
    public void memberVoiceJoinAndLeave(GuildVoiceUpdateEvent event) {
        if (event.getGuild().getIdLong() != guildId) return;
        if (event.getChannelJoined() != null && event.getChannelLeft() != null) return;
        AudioChannelUnion unionChannel = event.getChannelJoined() != null ? event.getChannelJoined() : event.getChannelLeft();
        if (unionChannel == null) return;
        VoiceChannel channel = unionChannel.getType() == ChannelType.VOICE ? unionChannel.asVoiceChannel() : null;
        if (channel == null) return;

        scheduleStatusUpdateIfAutomated(channel, DEFAULT_SCHEDULED_UPDATE_DELAY);
    }

    @SubscribeEvent
    public void __(UserUpdateOnlineStatusEvent event) {
        if (!event.getNewOnlineStatus().equals(OnlineStatus.IDLE)) return;
        if (event.getGuild().getIdLong() != guildId) return;
        GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null) return;
        VoiceChannel channel = voiceState.getChannel() == null ? null : voiceState.getChannel().getType() == ChannelType.VOICE ? voiceState.getChannel().asVoiceChannel() : null;
        if (channel == null) return;

        scheduleStatusUpdateIfAutomated(channel, DEFAULT_SCHEDULED_UPDATE_DELAY);
    }

    private void scheduleStatusUpdate(VoiceChannel channel, int scheduleDelayMillis) {
        if (scheduledUpdates.contains(channel)) return;
        scheduledUpdates.add(channel);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String newStatus = calculateNewChannelStatus(channel);

                if (!channel.getStatus().equals(newStatus))
                    channel.modifyStatus(newStatus).queue();
                scheduledUpdates.remove(channel);
            }
        }, scheduleDelayMillis);
    }

    private void scheduleStatusUpdateIfAutomated(VoiceChannel channel, int scheduleDelayMillis) {
        for (Activity.ActivityType type : Activity.ActivityType.values()) {
            if (!channel.getStatus().isEmpty() && (!channel.getStatus().startsWith(StringUtils.capitalizeFirst(type.name())) && !channel.getStatus().equals("All idle")))
                continue;

            scheduleStatusUpdate(channel, scheduleDelayMillis);
            break;
        }
    }

    private String calculateNewChannelStatus(VoiceChannel channel) {
        Map<String, Short> activityCount = new HashMap<>();

        if (channel.getMembers().isEmpty()) return "";

        int idleMembers = 0;
        for (Member member : channel.getMembers()) {
            if (member.getOnlineStatus().equals(OnlineStatus.IDLE)) {
                idleMembers++;
                continue;
            }

            for (Activity activity : member.getActivities()) {
                if (activity.getType() == Activity.ActivityType.CUSTOM_STATUS) continue;

                String activityContent = StringUtils.capitalizeFirst(activity.getType().name()) + " " + activity.getName();
                activityCount.put(activityContent, (short) (activityCount.getOrDefault(activityContent, (short) 0) + 1));
            }
        }

        if (idleMembers == channel.getMembers().size())
            return "All idle";

        String mostFrequentActivity = null;
        short maxCount = 0;
        List<String> allActivities = new ArrayList<>();

        for (Map.Entry<String, Short> entry : activityCount.entrySet()) {
            if (!allActivities.contains(entry.getKey()))
                allActivities.add(entry.getKey());
            short count = entry.getValue();
            if (count <= maxCount) continue;

            maxCount = count;
            mostFrequentActivity = entry.getKey();
        }

        if (maxCount > 1)
            return mostFrequentActivity;

        if (!allActivities.isEmpty()) {
            if (allActivities.size() == 1) {
                randomChannels.remove(channel);

                return allActivities.get(0);
            } else if (allActivities.size() == 2) {
                if (allActivities.get(0).equals(channel.getStatus())) {
                    return allActivities.get(1);
                } else {
                    return allActivities.get(0);
                }
            } else {
                randomChannels.add(channel);

                int currentStatusIndex = allActivities.indexOf(channel.getStatus());
                String randomActivity;
                if (currentStatusIndex != -1) {
                    int randomIndex = new Random().nextInt(allActivities.size() - 1);
                    if (randomIndex >= currentStatusIndex) randomIndex++;
                    randomActivity = allActivities.get(randomIndex);
                } else {
                    randomActivity = allActivities.get(new Random().nextInt(allActivities.size()));
                }

                return randomActivity;
            }
        } else {
            randomChannels.remove(channel);
        }

        return "";
    }

}