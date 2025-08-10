package com.denied403.Hardcourse.Discord.Tickets;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class SendTicketPanel {

    public void run(SlashCommandInteractionEvent e) {
        Guild guild = e.getGuild();
        if (guild == null) {
            e.reply("❌ This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }

        TextChannel targetChannel = e.getOption("channel").getAsChannel().asTextChannel();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎫 Open a Ticket");
        embed.setDescription("Please select the reason for your ticket:");
        embed.setColor(Color.BLUE);
        embed.setThumbnail(guild.getIconUrl());

        Button application = Button.success("ticket:application", "Application").withEmoji(Emoji.fromUnicode("🛡️"));
        Button appeal = Button.primary("ticket:appeal", "Appeal").withEmoji(Emoji.fromUnicode("❓"));
        Button general = Button.secondary("ticket:general", "Support Ticket").withEmoji(Emoji.fromUnicode("🎟️"));

        targetChannel.sendMessageEmbeds(embed.build())
                .addActionRow(application, appeal, general)
                .queue(
                        success -> e.reply("✅ Ticket panel sent to " + targetChannel.getAsMention()).setEphemeral(true).queue(),
                        failure -> e.reply("❌ Failed to send ticket panel.").setEphemeral(true).queue()
                );
    }

    public static OptionData channel() {
        return new OptionData(OptionType.CHANNEL, "channel", "The channel to send the ticket panel to", true)
                .setChannelTypes(ChannelType.TEXT)
                .setRequired(true);
    }
}
