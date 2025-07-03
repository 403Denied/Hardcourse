package com.denied403.hardcoursecheckpoints.Utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

public class Playtime {
    public static String getPlaytime(OfflinePlayer player) {
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);

        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Calculate the remaining hours, minutes, and seconds
        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        return String.format("%d Days, %02d Hours, %02d Minutes, %02d Seconds", days, hours, minutes, seconds);
    }
    public static String getPlaytimeShort(OfflinePlayer player) {
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);

        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Calculate the remaining hours, minutes, and seconds
        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        return String.format("%dd, %02dh, %02dm, %02ds", days, hours, minutes, seconds);
    }
}
