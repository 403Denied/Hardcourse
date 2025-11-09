package com.denied403.Hardcourse.Discord.Commands;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.denied403.Hardcourse.Utils.Playtime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.transfemme.dev.core403.Punishments.Utils.TimeUtils.formatUnixTimestamp;

public class Info {
    private static CheckpointDatabase database;

    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    static OptionMapping typeOption;
    public static OptionData infoType = new OptionData(OptionType.STRING, "info_type", "Type of information").addChoices(
            new Command.Choice("Server", "server"),
            new Command.Choice("Player", "player")
    );
    public static OptionData playerName = new OptionData(OptionType.STRING, "player_name", "Name of the player");
    static OptionMapping nameOption;
    public static String getUptime(){
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60)) % 24;
        long days = uptime / (1000 * 60 * 60 * 24);
        return String.format("%d days, %02d hours, %02d minutes, %02d seconds", days, hours, minutes, seconds);
    }
    public static double getUsedMemory(){
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double usedMemoryInMB = (double) usedMemory / (1024 * 1024);
        return Math.round(usedMemoryInMB * 100) / 100.0;
    }
    public static void run(SlashCommandInteractionEvent event){
        typeOption = event.getOption("info_type");
        if(typeOption == null) {
            event.reply("Please provide a type!").setEphemeral(true).queue();
            return;
        }
        String type = typeOption.getAsString();
        if(type.equals("server")) {
            ArrayList<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            String playersList = String.join(", ", playerNames);
            try {
                EmbedBuilder serverEmbed = new EmbedBuilder();
                serverEmbed.setThumbnail(Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getIconUrl()));
                serverEmbed.setTitle("Server Info • Hardcourse");
                serverEmbed.addField("Server IP", isDev() ? "hardcourse.dev.falixsrv.me" : "hardcourse.minehut.gg", false);
                serverEmbed.addField("Server Version", Bukkit.getVersion(), false);
                serverEmbed.addField("Server Uptime", getUptime(), false);
                serverEmbed.addField("Server TPS", String.format("%.2f", Bukkit.getTPS()[0]), false);
                serverEmbed.addField("Server Memory", getUsedMemory() + "MB", false);
                serverEmbed.addField("Players", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), false);
                serverEmbed.addField("Online Players", playersList, false);
                serverEmbed.setFooter("Requested by " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());
                event.replyEmbeds(serverEmbed.build()).setEphemeral(true).queue();
                return;
            } catch(Exception e){
                event.reply("An error occurred while fetching server info. Please send this to an administrator: ```" + e.getMessage() + "```").setEphemeral(true).queue();
                return;
            }
        }
        nameOption = event.getOption("player_name");
        if(nameOption == null) {
            event.reply("Please provide a player name!").setEphemeral(true).queue();
            return;
        }
        String name = nameOption.getAsString();
        Player onlineTarget = Bukkit.getPlayerExact(name);
        OfflinePlayer offlinePlayer = (onlineTarget != null) ? onlineTarget : Bukkit.getOfflinePlayer(name);
        if(!offlinePlayer.hasPlayedBefore()) {
            event.reply("This player has never played on the server!").setEphemeral(true).queue();
            return;
        }
        UUID uuid = offlinePlayer.getUniqueId();
        EmbedBuilder playerEmbed = new EmbedBuilder();
        playerEmbed.setThumbnail(Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getIconUrl()));
        String fullLevelString;
        Double level = database.getLevel(uuid);

        if (level == null) {
            fullLevelString = "Not Migrated";
        } else {
            String levelString = level.intValue() + "";
            Integer season = database.getSeason(uuid);
            fullLevelString = (season != null ? season + "-" : "") + levelString;
        }

        try {
            playerEmbed.setTitle(offlinePlayer.getName());
            playerEmbed.setFooter("Requested by " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());
            playerEmbed.addField("Level", fullLevelString, false);
            if(isDev()) {
                playerEmbed.addField("Points", String.valueOf(database.getPoints(uuid)), false);
            }
            playerEmbed.addBlankField(false);
            playerEmbed.addField("First Joined", formatUnixTimestamp(offlinePlayer.getFirstPlayed()) + " (" + formatDurationAgo(offlinePlayer.getFirstPlayed()) + ")", false);
            playerEmbed.addField("Playtime", Playtime.getPlaytime(offlinePlayer), false);
            if(!offlinePlayer.isOnline()) {
                playerEmbed.addField("Last Seen", formatUnixTimestamp(offlinePlayer.getLastSeen()) + " (" + formatDurationAgo(offlinePlayer.getLastSeen()) + ")", false);
            } else {
                playerEmbed.addField("Online Since", formatUnixTimestamp(offlinePlayer.getLastLogin()), false);
            }
            event.replyEmbeds(playerEmbed.build()).setEphemeral(true).queue();
        } catch (NoClassDefFoundError | Exception e) {
            event.reply("An error occurred while fetching player info. Please send this to an administrator: ```" + e.getMessage() + "```").setEphemeral(true).queue();
        }
    }
    public static String formatDurationAgo(long pastUnixTime) {
        long seconds = (System.currentTimeMillis() - pastUnixTime) / 1000;

        long years = seconds / (365 * 24 * 60 * 60);
        seconds %= (365 * 24 * 60 * 60);

        long months = seconds / (30 * 24 * 60 * 60);
        seconds %= (30 * 24 * 60 * 60);

        long weeks = seconds / (7 * 24 * 60 * 60);
        seconds %= (7 * 24 * 60 * 60);

        long days = seconds / (24 * 60 * 60);
        seconds %= (24 * 60 * 60);

        long hours = seconds / (60 * 60);
        seconds %= (60 * 60);

        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        if (years > 0) sb.append(years).append(" year").append(years > 1 ? "s" : "").append(", ");
        if (months > 0) sb.append(months).append(" month").append(months > 1 ? "s" : "").append(", ");
        if (weeks > 0) sb.append(weeks).append(" week").append(weeks > 1 ? "s" : "").append(", ");
        if (days > 0) sb.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        if (hours > 0) sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        if (minutes > 0) sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(", ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append(" second").append(seconds != 1 ? "s" : "");

        String result = sb.toString().replaceAll(", $", "");

        return result + " ago";
    }
}
