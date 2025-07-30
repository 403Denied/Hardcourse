package com.denied403.hardcoursecheckpoints.Scoreboard;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import static com.denied403.hardcoursecheckpoints.Points.PointsManager.getPoints;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;
import static com.denied403.hardcoursecheckpoints.Utils.Playtime.getPlaytimeShort;

public class ScoreboardMain {
    private static CheckpointDatabase database;

    public static void initialize(CheckpointDatabase db) {
        database = db;
    }

    private static final String OBJECTIVE_NAME = "hardcourse";

    public static void initScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Colorize("&c&lHARDCOURSE"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        createLine(board, obj, "season", "&fSeason:", 15);
        createLine(board, obj, "level", "&fLevel:", 14);
        createLine(board, obj, "playtime", "&fPlaytime:", 13);
        createLine(board, obj, "deaths", "&fDeaths:", 12);
        createLine(board, obj, "points", "&fPoints:", 11);
        obj.getScore("  ").setScore(10);
        createLine(board, obj, "players", "&fPlayers:", 9);
        createLine(board, obj, "tps", "&fTPS:", 8);
        createLine(board, obj, "ip", "&fIP:", 7);

        player.setScoreboard(board);
    }

    private static void createLine(Scoreboard board, Objective obj, String key, String prefix, int score) {
        Team team = board.getTeam(key);
        if (team == null) team = board.registerNewTeam(key);

        String entry =  "§" + Integer.toHexString(score % 16) + "§" + Integer.toHexString((score + 1) % 16);

        team.addEntry(entry);
        team.prefix(Colorize(prefix));
        obj.getScore(entry).setScore(score);
    }

    public static void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();

        String world = formatWorldName(player.getWorld().getName());
        String level = database.getLevel(player.getUniqueId()).toString().replace(".0", "");
        String deaths = String.valueOf(player.getStatistic(org.bukkit.Statistic.DEATHS));
        String playtime = getPlaytimeShort(player);
        int points = getPoints(player.getUniqueId());
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        int tps = (int) Math.round(Bukkit.getTPS()[0]);

        updateTeam(board, "season", " &c" + world);
        updateTeam(board, "level", " &c" + level);
        updateTeam(board, "playtime", " &c" + playtime);
        updateTeam(board, "deaths", " &c" + deaths);
        updateTeam(board, "points", " &c" + points);
        updateTeam(board, "players", " &c" + online + "/" + max);
        updateTeam(board, "tps", " &c" + tps);
        updateTeam(board, "ip", " &chardcourse.dev.falixsrv.me");
    }

    private static void updateTeam(Scoreboard board, String key, String suffix) {
        Team team = board.getTeam(key);
        if (team != null) {
            team.suffix(Colorize(suffix));
        }
    }

    private static String formatWorldName(String rawWorldName) {
        if (rawWorldName == null) return "Unknown";
        return switch (rawWorldName.toLowerCase()) {
            case "season1" -> "1";
            case "season2" -> "2";
            case "season3" -> "3";
            case "season4" -> "4";
            case "creativeplots" -> "Plots";
            default -> rawWorldName;
        };
    }
}
