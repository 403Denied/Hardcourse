package com.denied403.hardcoursecheckpoints.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.getHighestCheckpoint;
import static com.denied403.hardcoursecheckpoints.Points.PointsManager.getPoints;
import static com.denied403.hardcoursecheckpoints.Utils.Playtime.getPlaytimeShort;

public class ScoreboardMain {
    private static String formatWorldName(String rawWorldName) {
        if (rawWorldName == null) return "Unknown";
        switch (rawWorldName.toLowerCase()) {
            case "season1":
                return "1";
            case "season2":
                return "2";
            case "season3":
                return "3";
            case "season4":
                return "4";
            case "creativeplots":
                return "Plots";
            default:
                return rawWorldName;
        }
    }
    public static void setScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("hardcourse", "dummy", ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        int line = 15;
        String world = formatWorldName(player.getWorld().getName());
        String level = getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "");
        String deaths = String.valueOf(player.getStatistic(org.bukkit.Statistic.DEATHS));
        obj.getScore(" ").setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fSeason: &c" + world)).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fLevel: &c" + level)).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fPlaytime: &c" + getPlaytimeShort(player))).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fDeaths: &c" + deaths)).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fPoints: &c" + getPoints(player.getUniqueId()))).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', " ")).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fPlayers: &c" + Bukkit.getServer().getOnlinePlayers().size() + "&f/&c" + Bukkit.getServer().getMaxPlayers())).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fTPS: &c" + Math.round(Bukkit.getTPS()[0]))).setScore(line--);
        obj.getScore(ChatColor.translateAlternateColorCodes('&', "&fIP: &chardcourse.dev.falixsrv.me")).setScore(line);
        player.setScoreboard(board);
    }
}