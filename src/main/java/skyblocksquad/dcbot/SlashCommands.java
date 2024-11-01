package skyblocksquad.dcbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import skyblocksquad.dcbot.util.Project;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//todo: update & refactor
public class SlashCommands {

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("sendpingroles")) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("News Ping")
                    .setColor(Color.BLUE)
                    .setDescription("Get news notifications")
                    .build();

            event.getChannel()
                    .sendMessageEmbeds(embed)
                    .setActionRow(
                            Button.secondary("pingroles-news", "Toggle Role")
                    )
                    .queue(message -> event.reply("Ping roles selection message sent").setEphemeral(true).queue());
        } else if (event.getName().equals("downloads")) {
            if (event.getSubcommandName() != null)
                switch (event.getSubcommandName()) {
                    case "add" -> {
                        ActionRow titleInput = ActionRow.of(
                                TextInput.create("title", "Project Title", TextInputStyle.SHORT)
                                        .setRequired(true)
                                        .build()
                        );

                        ActionRow thumbnailInput = ActionRow.of(
                                TextInput.create("thumbnail", "Thumbnail URL", TextInputStyle.SHORT)
                                        //.setRequired(true)
                                        .build()
                        );

                        ActionRow videoUrlInput = ActionRow.of(
                                TextInput.create("videoUrl", "Video URL", TextInputStyle.SHORT)
                                        //.setRequired(true)
                                        .build()
                        );

                        ActionRow pmcUrlInput = ActionRow.of(
                                TextInput.create("pmcUrl", "Planet Minecraft URL", TextInputStyle.SHORT)
                                        .setRequired(true)
                                        .build()
                        );

                        ActionRow mcVersionInput = ActionRow.of(
                                TextInput.create("mcVersion", "Minecraft Version", TextInputStyle.SHORT)
                                        .setRequired(true)
                                        .build()
                        );

                        /*ActionRow downloadUrlInput = ActionRow.of(
                                TextInput.create("downloadUrl", "Direct Download URL", TextInputStyle.SHORT)
                                        .setRequired(true)
                                        .build()
                        );*/

                        event.replyModal(
                                Modal.create("downloads-add-download", "Add Download Project")
                                        .addComponents(titleInput, thumbnailInput, videoUrlInput, pmcUrlInput, mcVersionInput)
                                        .build()
                        ).queue();
                    }
                    case "remove" -> {
                        String title = event.getOption("title", OptionMapping::getAsString);

                        Main.getProjectsConfig().getProjects().removeIf(project -> project.getTitle().equals(title));
                        Main.getProjectsConfig().save();

                        event.reply("Project removed successfully!").setEphemeral(true).queue(interactionHook ->
                                interactionHook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
                        );
                    }
                    case "send" -> {
                        event.deferReply().queue(interactionHook -> {

                            List<Project> projects = Main.getProjectsConfig().getProjects();
                            List<MessageEmbed> embeds = new ArrayList<>();

                            for (Project project : projects) {
                                EmbedBuilder embedBuilder = new EmbedBuilder()
                                        .setTitle(project.getTitle())
                                        .setColor(Color.BLUE)
                                        .setThumbnail(project.getThumbnail())
                                        .addField("Video", (project.getVideoUrl().startsWith("http") ? "<" : "") + project.getVideoUrl() + (project.getVideoUrl().startsWith("http") ? ">" : ""), false)
                                        .addField("Planet Minecraft", "<" + project.getPmcUrl() + ">", false)
                                        .addField("MC Version", project.getMcVersion(), false);

                                if (project.getDownloadUrl() != null)
                                    embedBuilder.addField("Direct Download", "<" + project.getDownloadUrl() + ">", false);

                                embeds.add(embedBuilder.build());
                            }

                            for (MessageEmbed embed : embeds)
                                event.getChannel().sendMessageEmbeds(embed).setSuppressedNotifications(true).queue();

                            MessageEmbed infoEmbed = new EmbedBuilder()
                                    .setTitle("Downloads")
                                    .setColor(Color.BLUE)
                                    .setDescription("Here you can find most of the downloads to our projects")
                                    .addField("Information", "All our maps are for Minecraft JAVA only!\nAlso look at the Planet Minecraft Posts because there may be additional information.\nWe also would appreciate if you download our maps (not all have that) through the link on PM with Ad share", false)
                                    .build();

                            event.getChannel().sendMessageEmbeds(infoEmbed).queue();

                            interactionHook.deleteOriginal().queue();
                        });
                    }
                }
        } else if (event.getName().equals("message")) {
            String message = event.getOption("message").getAsString();

            event.getChannel()
                    .sendMessage(message)
                    .queue(success -> {
                        event.reply("Message sent").setEphemeral(true).queue();

                        MessageEmbed embed = new EmbedBuilder()
                                .setTitle("Bot Message Sent")
                                .setColor(Color.BLUE)
                                .addField("User", event.getUser().getAsMention() + "(" + event.getUser().getName() + ")", false)
                                .addField("Channel", event.getChannel().getAsMention() + " (" + event.getChannel().getName() + ")", false)
                                .addField("Message", message, false)
                                .setFooter("")
                                .build();

                        TextChannel textChannel = event.getJDA().getTextChannelById(Main.getConfig().getLogsChannelId());
                        if (textChannel != null) {
                            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getConfig().isSilentLogMessages()).queue();
                        }
                    });
        } else if (event.getName().equals("clear")) {
            int amount = event.getOption("amount").getAsInt();

            MessageChannelUnion channel = event.getChannel();

            event.reply("Removing messages...").setEphemeral(true).queue(interactionHook ->
                    getMessagesAsync(channel, amount, messages ->
                            interactionHook.editOriginal("Removed " + channel.purgeMessages(messages).size() + " messages.").queue())
            );
        }
    }

    @SubscribeEvent
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "downloads" -> {
                if ("remove".equals(event.getSubcommandName())) {
                    List<Command.Choice> options = Main.getProjectsConfig().getProjects().stream()
                            .map(Project::getTitle)
                            .filter(title -> title.startsWith(event.getFocusedOption().getValue()))
                            .map(title -> new Command.Choice(title, title))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                }
            }
        }
    }

    @SubscribeEvent
    public void onModalInteraction(ModalInteractionEvent event) {
        switch (event.getModalId()) {
            case "downloads-add-download" -> {
                String title = event.getValue("title").getAsString();
                String videoUrl = event.getValue("videoUrl") != null ? event.getValue("videoUrl").getAsString() : "*none*";
                String pmcUrl = event.getValue("pmcUrl").getAsString();
                String mcVersion = event.getValue("mcVersion").getAsString();
                String thumbnail = event.getValue("thumbnail") != null ? event.getValue("thumbnail").getAsString() : null;

                Project newProject = new Project();
                newProject.setTitle(title);
                newProject.setVideoUrl(videoUrl);
                newProject.setPmcUrl(pmcUrl);
                newProject.setMcVersion(mcVersion);
                newProject.setThumbnail(thumbnail);
                newProject.setDateCreated(System.currentTimeMillis());

                Main.getProjectsConfig().getProjects().add(newProject);
                Main.getProjectsConfig().save();

                event.reply("Project '" + title + "' added successfully!").setEphemeral(true).queue(interactionHook ->
                        interactionHook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS)
                );
            }
        }
    }

    private void getMessagesAsync(MessageChannel channel, int amount, Consumer<List<Message>> callback) {
        channel.getIterableHistory().takeAsync(amount).thenAccept(messages -> {
            List<Message> filteredMessages = messages.stream()
                    .filter(m -> !m.isPinned())
                    .collect(Collectors.toList());
            callback.accept(filteredMessages);
        });
    }

}