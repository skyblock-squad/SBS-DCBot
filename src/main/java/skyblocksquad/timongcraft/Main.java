package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import skyblocksquad.timongcraft.util.FileHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static JDA jda;
    private static String token;
    private static String welcomeChannel;
    private static String applicationChannel;
    private static String logsChannel;
    private static boolean logsSilent;
    private static String memberRoleName;
    private static String betaTesterRoleName;

    public static void main(String[] args) {
        String configFileName = "config.yml";
        File configFile = new File("config.yml");

        if(!configFile.exists()) {
            createConfig(configFileName);
            System.out.println("Config created. Set the variables and run the bot again.");
            System.exit(0);
        }

        FileHandler configHandler = new FileHandler(configFileName);
        token = configHandler.getString("botToken");
        welcomeChannel = configHandler.getString("welcomeChannelId");
        applicationChannel = configHandler.getString("applicationChannelId");
        logsChannel = configHandler.getString("logsChannelId");
        logsSilent = configHandler.getBoolean("logsSilent");
        memberRoleName = configHandler.getString("MemberRole");
        betaTesterRoleName = configHandler.getString("BetaTesterRole");

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
        //jda.retrieveCommands().queue(commands -> { for (Command command : commands) { command.delete().queue(); }});

        jda.upsertCommand("applybetatester", "Apply as beta tester")
                .addOption(OptionType.STRING, "mcusername", "Your minecraft username (java)", true)
                .addOption(OptionType.STRING, "reason", "Reason why do you want to become a beta tester", false)
                .queue();

        System.out.println("[DC-Bot] Finished Initialization");
    }

    private static void createConfig(String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write("botToken: DISCORD_BOT_TOKEN\n");
            fileWriter.write("welcomeChannelId: WELCOME_CHANNEL_ID\n");
            fileWriter.write("applicationChannelId: APPLICATION_CHANNEL_ID\n");
            fileWriter.write("logsChannelId: LOGS_CHANNEL_ID\n");
            fileWriter.write("logsSilent: true\n");
            fileWriter.write("MemberRole: Member\n");
            fileWriter.write("BetaTesterRole: null\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JDA getJDA()  { return jda; }
    public static String getWelcomeChannel() {
        return welcomeChannel;
    }
    public static String getApplicationChannel() {
        return applicationChannel;
    }

    public static String getLogsChannel() {
        return logsChannel;
    }
    public static boolean getLogsSilent() {
        return logsSilent;
    }
    public static String getMemberRoleName() { return memberRoleName; }
    public static String getBetaTesterRoleName() { return betaTesterRoleName; }

}