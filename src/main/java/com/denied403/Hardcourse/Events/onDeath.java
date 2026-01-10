package com.denied403.Hardcourse.Events;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.jda;
import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;

public class onDeath implements Listener {
    @EventHandler
    public void onDeathEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        if(isDiscordEnabled()){
            ThreadChannel channel = jda.getThreadChannelById("1457271269413228574");
            if(channel == null) return;
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            channel.sendMessage("`[" + f.format(new Date()) + "] " + player.getName() + " died [#" + player.getStatistic(Statistic.DEATHS) + "]`").queue();
        }
    }
}
