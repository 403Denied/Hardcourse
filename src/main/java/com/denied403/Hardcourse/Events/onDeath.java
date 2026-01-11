package com.denied403.Hardcourse.Events;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.deathsChannel;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;

public class onDeath implements Listener {
    @EventHandler
    public void onDeathEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if(DiscordEnabled) {
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            deathsChannel.sendMessage("`[" + f.format(new Date()) + "] " + player.getName() + " died [#" + player.getStatistic(Statistic.DEATHS) + "]`").queue();
        }
    }
}
