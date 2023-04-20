package skyblocksquad.timongcraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Listeners extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setDescription(event.getUser().getAsMention())
                .setImage(event.getUser().getAvatarUrl())
                .setFooter("We're happy to meet you")
                .build();

        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getWelcomeChannel());
        if(textChannel != null)
            textChannel.sendMessage("Welcome " + event.getUser().getName() + " to the Skyblock Squad Discord server").setEmbeds(embed).queue();
        Role memberRole = event.getGuild().getRolesByName(Main.getMemberRoleName(), true).get(0);
        event.getGuild().addRoleToMember(event.getMember(), memberRole).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have the permission").setEphemeral(true).queue();
            return;
        }

        if(event.getComponentId().equals("accept")) {
            Role betaTesterRole = event.getGuild().getRolesByName(Main.getBetaTesterRoleName(), true).get(0);
            String mcUsername = null;
            String reason = null;
            MessageEmbed readembed = event.getMessage().getEmbeds().get(0);
            List<MessageEmbed.Field> fields = readembed.getFields();

            for (MessageEmbed.Field field : fields) {
                String name = field.getName();
                String value = field.getValue();

                if(name.equals("Minecraft Username")) {
                    mcUsername = value;
                    break;
                }
            }

            for (MessageEmbed.Field field : fields) {
                String name = field.getName();
                String value = field.getValue();

                if(name.equals("Reason")) {
                    reason = value;
                    break;
                }
            }

            final String finMcUsername = mcUsername;
            final String finReason = reason;
            String appliedUserId = event.getMessage().getEmbeds().get(0).getFooter().getText();
            event.getGuild().retrieveMemberById(appliedUserId).queue(member -> {
                member.getUser().openPrivateChannel().queue(privateChannel -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Accepted as Beta Tester")
                            .setColor(Color.GREEN)
                            .addField("DC-Server", "Skyblock Squad", false)
                            .addField("Info", "You can now have access to <#1086705968009719899> and <#1086707130695954472>", false)
                            .setFooter("accepted by " + event.getUser().getAsTag());

                    privateChannel.sendMessageEmbeds(embed.build()).queue(success -> {
                        event.getGuild().addRoleToMember(member, betaTesterRole).queue();

                        MessageEmbed msgEmbed = new EmbedBuilder()
                                .setTitle("Accepted Beta Tester")
                                .setColor(Color.BLUE)
                                .addField("Discord Username", member.getUser().getAsTag(), false)
                                .addField("Minecraft Username", finMcUsername, false)
                                .addField("User's reason", finReason, false)
                                .setFooter("accepted by " + event.getUser().getAsTag())
                                .build();

                        //event.getMessage().editMessage(MessageEditData.fromEmbeds(embed)).setActionRow().queue();
                        event.getMessage().delete().queue();
                        event.getChannel()
                                .sendMessageEmbeds(msgEmbed)
                                .setSuppressedNotifications(true)
                                .queue();

                        try { Files.write(Path.of("users.log"), (member.getId() + " " + finMcUsername + "\n").getBytes(), StandardOpenOption.APPEND); } catch (Exception e) { e.printStackTrace(); }
                    }, e -> {
                        if(e instanceof ErrorResponseException && ((ErrorResponseException) e).getErrorCode() == 50007) {
                            event.reply(member.getUser().getAsTag() + " has DMs disabled!\nAcceptation has been canceled").setEphemeral(true).queue();
                        }
                    });
                });
            }, failure -> event.reply("Couldn't get user").setEphemeral(true));



        } else if(event.getComponentId().equals("reject")) {
            event.getMessage().delete().queue();
        }
    }
}
