package com.denied403.Hardcourse.Utils;

import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

public class Playtime {
    public static String getPlaytime(OfflinePlayer player) {
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);

        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        days %= 7;
        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();

        appendTimeUnit(sb, weeks, "Week");
        appendTimeUnit(sb, days, "Day");
        appendTimeUnit(sb, hours, "Hour");
        appendTimeUnit(sb, minutes, "Minute");
        appendTimeUnit(sb, seconds, "Second");

        return sb.isEmpty() ? "0 Seconds" : sb.toString();
    }
    private static void appendTimeUnit(StringBuilder sb, long value, String unit) {
        if (value <= 0) return;
        if (!sb.isEmpty()) sb.append(", ");
        sb.append(value).append(" ").append(pluralize(unit, value));
    }
    private static String pluralize(String unit, long value) {
        return value == 1 ? unit : unit + "s";
    }
}
