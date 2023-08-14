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
import skyblocksquad.dcbot.util.FileHandler;

import java.io.*;

public class Main {
    private static JDA jda;
    private static String token;
    private static String welcomeChannel, logsChannel, voiceLogsChannel;
    private static boolean logsSilent;
    private static String memberRoleName;
    private static String pingRolesNewsRoleName;

    public static void main(String[] args) {
        String configFileName = "config.yml";
        File configFile = new File("config.yml");

        if (!configFile.exists()) {
            createConfig(configFileName);
            System.out.println("Config created. Set the variables and run the bot again.");
            System.exit(0);
        }

        FileHandler configHandler = new FileHandler(configFileName);
        token = configHandler.getString("botToken");
        welcomeChannel = configHandler.getString("welcomeChannelId");
        logsChannel = configHandler.getString("logsChannelId");
        voiceLogsChannel = configHandler.getString("voiceLogsChannelId");
        logsSilent = configHandler.getBoolean("logsSilent");
        memberRoleName = configHandler.getString("MemberRole");
        pingRolesNewsRoleName = configHandler.getString("PingRolesNewsRole");

        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.watching("on your applications"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new Listeners(), new SlashCommands(), new LoggingListeners())
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

        handleStop();

        System.out.println("[DC-Bot] Finished Initialization");
    }

    public static void handleStop() {
        new Thread(() -> {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while ((line = reader.readLine()) != null) {
                    if (!line.equalsIgnoreCase("stop")) {
                        System.out.println("[DC-Bot] Unknown command.");
                        continue;
                    }

                    System.out.println("[DC-Bot] Shutting down...");

                    if (jda != null) {
                        jda.shutdown();
                    }
                    reader.close();
                    System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void createConfig(String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write("botToken: DISCORD_BOT_TOKEN\n");
            fileWriter.write("welcomeChannelId: WELCOME_CHANNEL_ID\n");
            fileWriter.write("logsChannelId: LOGS_CHANNEL_ID\n");
            fileWriter.write("voiceLogsChannelId: VOICE_LOGS_CHANNEL_ID\n");
            fileWriter.write("logsSilent: true\n");
            fileWriter.write("MemberRole: Member\n");
            fileWriter.write("PingRolesNewsRole: null\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static String getWelcomeChannel() {
        return welcomeChannel;
    }

    public static String getLogsChannel() {
        return logsChannel;
    }

    public static String getVoiceLogsChannel() {
        return voiceLogsChannel;
    }

    public static boolean getLogsSilent() {
        return logsSilent;
    }

    public static String getMemberRoleName() {
        return memberRoleName;
    }

    public static String getPingRolesNewsRoleName() {
        return pingRolesNewsRoleName;
    }
}