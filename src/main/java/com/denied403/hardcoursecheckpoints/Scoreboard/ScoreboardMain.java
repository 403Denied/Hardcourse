package com.denied403.hardcoursecheckpoints.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.getHighestCheckpoint;
import static com.denied403.hardcoursecheckpoints.Points.PointsManager.getPoints;
import static com.denied403.hardcoursecheckpoints.Utils.Playtime.getPlaytimeShort;

public class ScoreboardMain {

    private static final String OBJECTIVE_NAME = "hardcourse";

    public static void initScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(OBJECTIVE_NAME, "dummy", ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.getScore("Season:").setScore(15);
        obj.getScore("Level:").setScore(14);
        obj.getScore("Playtime:").setScore(13);
        obj.getScore("Deaths:").setScore(12);
        obj.getScore("Points:").setScore(11);
        obj.getScore("  ").setScore(10);
        obj.getScore("Players:").setScore(9);
        obj.getScore("TPS:").setScore(8);
        obj.getScore("IP:").setScore(7);

        player.setScoreboard(board);
    }

    public static void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective(OBJECTIVE_NAME);
        if (obj == null) return;

        String world = formatWorldName(player.getWorld().getName());
        String level = getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "");
        String deaths = String.valueOf(player.getStatistic(org.bukkit.Statistic.DEATHS));
        String playtime = getPlaytimeShort(player);
        int points = getPoints(player.getUniqueId());
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        int tps = (int) Math.round(Bukkit.getTPS()[0]);

        updateEntry(board, "Season:", ChatColor.WHITE + "Season: " + ChatColor.RED + world);
        updateEntry(board, "Level:", ChatColor.WHITE + "Level: " + ChatColor.RED + level);
        updateEntry(board, "Playtime:", ChatColor.WHITE + "Playtime: " + ChatColor.RED + playtime);
        updateEntry(board, "Deaths:", ChatColor.WHITE + "Deaths: " + ChatColor.RED + deaths);
        updateEntry(board, "Points:", ChatColor.WHITE + "Points: " + ChatColor.RED + points);
        updateEntry(board, "Players:", ChatColor.WHITE + "Players: " + ChatColor.RED + online + "/" + max);
        updateEntry(board, "TPS:", ChatColor.WHITE + "TPS: " + ChatColor.RED + tps);
        updateEntry(board, "IP:", ChatColor.WHITE + "IP: " + ChatColor.RED + "hardcourse.dev.falixsrv.me");
    }

    private static void updateEntry(Scoreboard board, String oldEntry, String newEntry) {
        for (String entry : board.getEntries()) {
            if (ChatColor.stripColor(entry).startsWith(ChatColor.stripColor(oldEntry))) {
                board.resetScores(entry);
                break;
            }
        }

        Objective obj = board.getObjective(OBJECTIVE_NAME);
        if (obj != null) {
            obj.getScore(newEntry).setScore(getScoreValue(oldEntry));
        }
    }

    private static int getScoreValue(String entry) {
        switch (entry) {
            case "Season:": return 15;
            case "Level:": return 14;
            case "Playtime:": return 13;
            case "Deaths:": return 12;
            case "Points:": return 11;
            case "Players:": return 9;
            case "TPS:": return 8;
            case "IP:": return 7;
            default: return 1;
        }
    }

    private static String formatWorldName(String rawWorldName) {
        if (rawWorldName == null) return "Unknown";
        switch (rawWorldName.toLowerCase()) {
            case "season1": return "1";
            case "season2": return "2";
            case "season3": return "3";
            case "season4": return "4";
            case "creativeplots": return "Plots";
            default: return rawWorldName;
        }
    }
}
