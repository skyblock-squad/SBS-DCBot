package skyblocksquad.dcbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import skyblocksquad.dcbot.config.main.Config;
import skyblocksquad.dcbot.listeners.GeneralListeners;
import skyblocksquad.dcbot.listeners.LoggingListeners;
import skyblocksquad.dcbot.util.LoggingFormat;

import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Main {

    private static Logger logger;
    private static final Config config = new Config();
    private static JDA jda;

    public static void main(String[] args) {
        loadLogger();

        config.load();

        jda = JDABuilder.createDefault(config.getBotToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.customStatus("Is watching youtube"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(new GeneralListeners(), new SlashCommands(), new LoggingListeners())
                .disableCache(CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .build();

        //Reset slash commands
        //jda.retrieveCommands().queue(commands -> { for(Command command : commands) { command.delete().queue(); }});

        jda.upsertCommand("sendpingroles", "Sends a message with the ping roles")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .queue();

        jda.upsertCommand("senddownloads", "Sends a message with the downloads")
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

        handleConsole();

        logger.info("Initialized");
    }

    private static void loadLogger() {
        logger = Logger.getLogger("SBS-DCBot");
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LoggingFormat());
        logger.addHandler(handler);
    }

    private static void handleConsole() {
        new Thread(() -> {
            Scanner consoleScanner = new Scanner(System.in);
            String consoleInput;

            while ((consoleInput = consoleScanner.next()) != null) {
                if (!consoleInput.equalsIgnoreCase("stop")) {
                    logger.info("Unknown command.");
                    continue;
                }

                logger.info("Shutting down...");

                if (jda != null)
                    jda.shutdown();

                consoleScanner.close();
                System.exit(0);
            }
        }).start();
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }

    public static JDA getJDA() {
        return jda;
    }

}