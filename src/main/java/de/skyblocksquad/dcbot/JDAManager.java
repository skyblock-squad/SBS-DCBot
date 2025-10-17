package de.skyblocksquad.dcbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import de.skyblocksquad.dcbot.listeners.ChannelActivityManager;
import de.skyblocksquad.dcbot.listeners.GeneralListener;
import de.skyblocksquad.dcbot.listeners.LoggingListener;

public class JDAManager {

    public void initialize(String botToken) {
        try {
            JDA jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setActivity(Activity.customStatus("Is watching YouTube"))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListeners(this, new SlashCommands())
                    .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                    .build().awaitReady();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down JDA...");
                jda.shutdown();
            }));
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to initialize JDA", e);
        }
    }

    @SubscribeEvent
    public void load(ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(Main.getConfig().getGuildId());

        if (guild == null)
            throw new RuntimeException("Guild with id '" + Main.getConfig().getGuildId() + "' could not be found");

        event.getJDA().addEventListener(
                new GeneralListener(guild.getIdLong()),
                new LoggingListener(guild),
                new ChannelActivityManager(guild));

        setupCommands(event.getJDA());
    }

    private static void setupCommands(JDA jda) {
        //Reset slash commands
        //jda.retrieveCommands().queue(commands -> { for(Command command : commands) { command.delete().queue(); }});

        jda.upsertCommand("sendpingroles", "Sends a message with the ping roles")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        jda.upsertCommand("downloads", "Manages the downloads")
                .addSubcommands(
                        new SubcommandData("add", "Adds a download"),
                        new SubcommandData("remove", "Removes a download")
                                .addOption(OptionType.STRING, "title", "The title of the project", true, true),
                        new SubcommandData("send", "Sends a message with all downloads")
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        jda.upsertCommand("message", "Send a message through the bot")
                .addOption(OptionType.STRING, "message", "The message you want to send", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        jda.upsertCommand("clear", "Remove a specified amount of messages")
                .addOption(OptionType.INTEGER, "amount", "The amount of messages to delete", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();
    }

}