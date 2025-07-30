package com.denied403.hardcoursecheckpoints.Discord.Commands;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import com.denied403.hardcoursecheckpoints.Utils.Playtime;
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
                serverEmbed.setThumbnail(Objects.requireNonNull(event.getGuild().getIconUrl()));
                serverEmbed.setTitle("Server Info • Hardcourse");
                serverEmbed.addField("Server IP", "hardcourse.minehut.gg", false);
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
        UUID uuid = offlinePlayer.getUniqueId();
        if(offlinePlayer.getName() == null) {
            event.reply("This player has never played on the server!").setEphemeral(true).queue();
            return;
        }
        EmbedBuilder playerEmbed = new EmbedBuilder();
        playerEmbed.setThumbnail(Objects.requireNonNull(event.getGuild().getIconUrl()));
        String level = String.valueOf(database.getLevel(uuid)).replace(".0", "");
        level = database.getSeason(uuid).toString() + "-" + level;
        try {
            playerEmbed.setTitle(offlinePlayer.getName());
            playerEmbed.setFooter("Requested by " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());
            playerEmbed.setImage("https://crafatar.com/renders/body/" + uuid + "?default=MHF_Steve&overlay");
            playerEmbed.addField("Level", level, false);
            playerEmbed.addField("Playtime: ", Playtime.getPlaytime(offlinePlayer), false);
            event.replyEmbeds(playerEmbed.build()).queue();
        } catch (NoClassDefFoundError | Exception e) {
            event.reply("An error occurred while fetching player info. Please send this to an administrator: ```" + e.getMessage() + "```").setEphemeral(true).queue();
        }
    }
}
