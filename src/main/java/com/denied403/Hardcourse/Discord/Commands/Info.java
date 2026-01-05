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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.denied403.Hardcourse.Hardcourse.isDev;

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
        return "<t:" + (Instant.now().toEpochMilli() - uptime) / 1000 + ":f>";
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
            String playersList;
            if(playerNames.isEmpty()){
                playersList = "None";
            } else {
                playersList = String.join(", ", playerNames);
            }
            try {
                EmbedBuilder serverEmbed = new EmbedBuilder();
                serverEmbed.setThumbnail(Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getIconUrl()));
                serverEmbed.setTitle("Server Info • Hardcourse");
                serverEmbed.addField("Server IP", isDev() ? "hardcourse.dev.falixsrv.me" : "hardcourse.minehut.gg", false);
                serverEmbed.addField("Server Version", Bukkit.getVersion(), false);
                serverEmbed.addField("Online Since", getUptime(), false);
                serverEmbed.addField("Server TPS", String.format("%.2f", Bukkit.getTPS()[0]), false);
                serverEmbed.addField("Server Memory", getUsedMemory() + "MB", false);
                serverEmbed.addField("Players", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), false);
                serverEmbed.addField("Online Players", playersList, false);
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
        UUID uuid = offlinePlayer.getUniqueId();
        String linkedDiscord = null;
        String linkedUuidString = database.getDiscordId(uuid);
        if (linkedUuidString != null) {
            linkedDiscord = "<@" + linkedUuidString + ">";
        }
        if(!offlinePlayer.hasPlayedBefore()) {
            event.reply("This player has never played on the server!").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder playerEmbed = new EmbedBuilder();
        playerEmbed.setThumbnail("https://mc-heads.net/avatar/" + uuid + ".png");
        String fullLevelString;
        Double level = database.getLevel(uuid);

        if (level == null) {
            fullLevelString = "Not Migrated";
        } else {
            String levelString = String.valueOf(level).replace(".0", "");
            Integer season = database.getSeason(uuid);
            fullLevelString = (season != null ? season + "-" : "") + levelString;
        }

        try {
            playerEmbed.setTitle(offlinePlayer.getName());
            playerEmbed.addField("Level", fullLevelString, false);
            if(isDev()) {
                playerEmbed.addField("Points", String.valueOf(database.getPoints(uuid)), false);
            }
            playerEmbed.addBlankField(false);
            playerEmbed.addField("First Joined", "<t:" + offlinePlayer.getFirstPlayed() / 1000L + ":F>", false);
            playerEmbed.addField("Playtime", Playtime.getPlaytime(offlinePlayer), false);
            if(!offlinePlayer.isOnline()) {
                playerEmbed.addField("Last Seen", "<t:" + offlinePlayer.getLastSeen() / 1000L + ":F>", false);
            } else {
                playerEmbed.addField("Online Since", "<t:" +   offlinePlayer.getLastLogin() / 1000L + ":f>", false);
            }
            if(linkedDiscord != null){
                playerEmbed.addField("Discord", linkedDiscord, false);
            }
            event.replyEmbeds(playerEmbed.build()).setEphemeral(true).queue();
        } catch (NoClassDefFoundError | Exception e) {
            event.reply("An error occurred while fetching player info. Please send this to an administrator: ```" + e.getMessage() + "```").setEphemeral(true).queue();
        }
    }
}
