package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.security.auth.login.LoginException;

public class Main {
    private static JDA jda;
    private static String token = "OTIwNjUzMjcxMjA5NzQyNDA2.GE4Tfu.L9LHy0G8VPeXYN5yiJ5kITfk2j3yK15zhExVWM";

    public static void main(String[] args) {
        jda = JDABuilder.createDefault(token)
                .setActivity(Activity.watching("on your messages"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new Listeners(), new SlashCommands())
                .build();

        jda.upsertCommand("getbetatester", "Get a beta tester")
                .addOption(OptionType.STRING, "mcusername", "Your minecraft username (java)", true)
                .addOption(OptionType.STRING, "reason", "Reason why do you want to be a beta tester", true)
                .queue();
        jda.upsertCommand("embed", "Send an embed")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOption(OptionType.CHANNEL, "channel", "The channel to send the embed to", true)
                .queue();

    }
}