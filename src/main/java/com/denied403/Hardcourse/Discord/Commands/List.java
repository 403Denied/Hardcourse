package com.denied403.Hardcourse.Discord.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;

import static com.denied403.Hardcourse.Utils.ColorUtil.stripAllColors;

public class List {
    public static void run(SlashCommandInteractionEvent event) {
        java.util.List<String> onlinePlayers = new ArrayList<>();
        java.util.List<String> staffMembers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = "`" + stripAllColors(player.displayName()) + "`";
            if (player.hasPermission("hardcourse.jrmod")) {
                staffMembers.add(name);
            } else {
                onlinePlayers.add(name);
            }
        }

        String playersList = onlinePlayers.isEmpty() ? "No players online!" : String.join(", ", onlinePlayers);
        String staffList = staffMembers.isEmpty() ? "No staff members online!" : String.join(", ", staffMembers);

        EmbedBuilder serverEmbed = new EmbedBuilder();
        serverEmbed.setThumbnail(event.getGuild().getIconUrl());
        serverEmbed.setTitle("Player list");
        serverEmbed.addField("Server IP", "- Hardcourse.minehut.gg", true);
        serverEmbed.addField("Staff online (" + staffMembers.size() + ")", staffList, false);
        serverEmbed.addField("Players online (" + onlinePlayers.size() + ")", playersList, false);
        serverEmbed.setFooter("Requested by " + event.getUser().getAsTag());
        serverEmbed.setColor(Color.blue);
        event.replyEmbeds(serverEmbed.build()).queue();
    }
}

