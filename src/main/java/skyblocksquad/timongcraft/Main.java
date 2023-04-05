package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

public class Main {
    private static JDA jda;
    private static String token = "MTA5MjA4NjE1NzAyNzg1NjU1NQ.G7w6ta.dRh78oqEpUXRWVm8rU7_a1OciiEP-jkT7rGSLs";

    public static void main(String[] args) {
        jda = JDABuilder.createDefault(token)
                .setActivity(Activity.watching("on your applications"))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(new Listeners(), new SlashCommands())
                .build();

        //Reset slash commands
        //jda.retrieveCommands().queue(commands -> { for (Command command : commands) { command.delete().queue(); }});

        jda.upsertCommand("applybetatester", "Apply as beta tester")
                .addOption(OptionType.STRING, "mcusername", "Your minecraft username (java)", true)
                .addOption(OptionType.STRING, "reason", "Reason why do you want to become a beta tester", true)
                .queue();

        System.out.println("[DC-Bot] Finished Initialization");
    }
}