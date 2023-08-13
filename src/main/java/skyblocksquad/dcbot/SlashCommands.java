package skyblocksquad.dcbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;


public class SlashCommands extends ListenerAdapter {
    @Override
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
        } else if (event.getName().equals("senddownloads")) {
            MessageEmbed downloadsEmbed = new EmbedBuilder()
                    .setTitle("Downloads")
                    .setColor(Color.BLUE)
                    .setDescription("Here you can find most of the downloads to our projects")
                    .addField("Information", "All our maps are for Minecraft JAVA only!\nAlso look at the Planet Minecraft Posts because there may be additional information.\nWe also would appreciate if you download our maps (not all have that) through the link on PM with Ad share", false)
                    .build();

            MessageEmbed blockSumoUpdatedProjectEmbed = new EmbedBuilder()
                    .setTitle("Block Sumo")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2023/116/16979230_l.webp")
                    .setDescription("")
                    //.addField("Video", "<>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/block-sumo-minigame-updated-1-20-1>", false)
                    .addField("MC Version", "1.20.1", false)
                    .addField("Direct Download", "<https://static.planetminecraft.com/files/resource_media/schematic/7by-cskyblocksquad-8-unzip-me.zip>", false)
                    .build();

            MessageEmbed minehuhnProjectEmbed = new EmbedBuilder()
                    .setTitle("Moorhuhn in Minecraft")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2023/306/16717201-thumbnail_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/-SxJYDNCRSs>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/we-made-moorhuhn-in-minecraft>", false)
                    .addField("MC Version (created)", "1.19.4", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1PnfSlbcNDkvNXgD0Ei2-6JlfDj8KrRMI/view>", false)
                    .build();

            MessageEmbed fireworkShow2022ProjectEmbed = new EmbedBuilder()
                    .setTitle("Firework show (2022-2023)")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2022/382/16383463-spoiler-f_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/cu3GD_3nKxU>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/the-coolest-lightshow-in-minecraft>", false)
                    .addField("MC Version (created)", "1.19.2", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1npbehMBdoX_zoN2l_4bl_HoZl-v_wJeQ/view>", false)
                    .build();

            MessageEmbed mcKartProjectEmbed = new EmbedBuilder()
                    .setTitle("MC Kart")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2022/659/16008513-thumbnail_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/HGrWINVRtGM>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/mario-kart-in-minecraft-1-19>", false)
                    .addField("MC Version (created)", "1.19.3", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1WUEd87PWf2Wmqqr-Nqgh7duIWoSqOPjU/view>", false)
                    .build();

            MessageEmbed blockSumoProjectEmbed = new EmbedBuilder()
                    .setTitle("Block Sumo [old]")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2022/315/16118434-thumbnail-blocksumo_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/fD3piFgyNpg>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/block-sumo-recreated-by-skyblocksquad-pvp-map>", false)
                    .addField("MC Version (created)", "1.19.2", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1sRIGt9KS-TPwVn6OSjYUDs86s3LL1DaE/view>", false)
                    .build();

            MessageEmbed parkourWarriorProjectEmbed = new EmbedBuilder()
                    .setTitle("Parkour Warrior")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://cdn.discordapp.com/attachments/1026105978166464512/1101789045236957194/hqdefault.jpg")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/QooUJC_JH6s>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/we-recreated-ninja-warrior-in-minecraft>", false)
                    .addField("MC Version (created)", "1.19", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1AV5bcxZLqoQS3ysq5YviifE-alJqCQJ1/view>", false)
                    .build();

            MessageEmbed liyueProjectEmbed = new EmbedBuilder()
                    .setTitle("Liyue Harbour from Genshin Impact")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2022/249/15372762-thumliyue_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/lnQRGL-yJl4>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/liyue-harbour-from-genshin-impact-in-minecraft-1-1-scale>", false)
                    .addField("MC Version (created)", "1.18.1", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/10DcaTzAyv8d1o2-kRrzlVPjIFeaDTX4I/view>", false)
                    .build();

            MessageEmbed moonstadtProjectEmbed = new EmbedBuilder()
                    .setTitle("Mondstadt from Genshin Impact")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2022/435/15361938-thum_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/xB22z0evtIk>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/city-of-mondstadt-from-genshin-impact-in-minecraft-1-1-scale>", false)
                    .addField("MC Version (created)", "1.17.1", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/19NjhJGQWgBhqgYVN8STh_NZ4Cn_83d5f/view>", false)
                    .build();

            MessageEmbed fireworkShow2021ProjectEmbed = new EmbedBuilder()
                    .setTitle("Firework show (2021-2022)")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/project/2021/346/15252262_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/lWw9v3Sr4Tw>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/we-made-an-epic-minecraft-firework-show>", false)
                    .addField("MC Version (created)", "1.17.1", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1jcGCx92U_Fu8im7yF3RohIBU03F9Fuw8/view>", false)
                    .build();

            MessageEmbed phantomProjectEmbed = new EmbedBuilder()
                    .setTitle("The Phantom")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://cdn.discordapp.com/attachments/1026105978166464512/1101792410834579566/maxresdefault_2.jpg")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/jZVusmJF5dE>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/the-phantom-5357966>", false)
                    .addField("MC Version (created)", "1.17.1", false)
                    .addField("Direct Map Download", "<https://drive.google.com/file/d/1Zfxnj4fkyUKVVRvXRKmm158x1944wWri/view>", false)
                    .addField("Direct Resource Pack Download", "<https://drive.google.com/file/d/1LSDHeb3_XLzzAK1E41EGc2m8-XCIJZuT/view>", false)
                    .build();

            MessageEmbed teleporterProjectEmbed = new EmbedBuilder()
                    .setTitle("Teleporter in Vanilla Minecraft")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://static.planetminecraft.com/files/image/minecraft/data-pack/2021/292/14976430-thum_l.webp")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/rmRZzQnC0J8>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/data-pack/teleporter-in-minecraft-vanilla>", false)
                    .addField("MC Version (created)", "1.17", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/10yiGqi0joZLssM1S2C-kZMSG-g0RNLiN/view>", false)
                    .build();

            MessageEmbed capsuleProjectEmbed = new EmbedBuilder()
                    .setTitle("The Capsule")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://cdn.discordapp.com/attachments/1026105978166464512/1101789716615012402/maxresdefault.jpg")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/qehxkV46LnA>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/the-capsule-by-sbs>", false)
                    .addField("MC Version (created)", "1.16.1", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1otCwAdkd6Fw3EkGxgliT-IM2oNvBN3TS/view>", false)
                    .build();

            MessageEmbed easterspecialProjectEmbed = new EmbedBuilder()
                    .setTitle("Easter Special (2021)")
                    .setColor(Color.BLUE)
                    .setThumbnail("https://cdn.discordapp.com/attachments/1026105978166464512/1101789925722038292/maxresdefault_1.jpg")
                    .setDescription("")
                    .addField("Video", "<https://youtu.be/O5v91ZZDxeE>", false)
                    .addField("Planet Minecraft", "<https://www.planetminecraft.com/project/easter-special-map>", false)
                    .addField("MC Version (created)", "1.16.1", false)
                    .addField("Direct Download", "<https://drive.google.com/file/d/1JazUizsQaG3b1XBl0nNwYWT-WkbFqY-S/view>", false)
                    .build();


            event.getChannel()
                    .sendMessageEmbeds(downloadsEmbed, blockSumoUpdatedProjectEmbed, minehuhnProjectEmbed, fireworkShow2022ProjectEmbed, mcKartProjectEmbed, blockSumoProjectEmbed, parkourWarriorProjectEmbed, liyueProjectEmbed, moonstadtProjectEmbed, fireworkShow2021ProjectEmbed)
                    .queue(message -> event.reply("Downloads message sent").setEphemeral(true).queue());
            event.getChannel().sendMessageEmbeds(phantomProjectEmbed, teleporterProjectEmbed, capsuleProjectEmbed, easterspecialProjectEmbed).queue();
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

                        TextChannel textChannel = Main.getJDA().getTextChannelById(Main.getLogsChannel());
                        if (textChannel != null) {
                            textChannel.sendMessageEmbeds(embed).setSuppressedNotifications(Main.getLogsSilent()).queue();
                        }
                    });
        }
    }

}
