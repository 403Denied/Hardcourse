package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;
import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;

public class onQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(DiscordEnabled) {
            sendMessage(e.getPlayer(), null, "leave", null, null);
            sendMessage(e.getPlayer(), null, "logs", "quit", null);
        }
        Double highestLevel = checkpointDatabase.getLevel(e.getPlayer().getUniqueId());
        int season = checkpointDatabase.getSeason(e.getPlayer().getUniqueId());
        if(highestLevel <= 3 && season == 1){
            checkpointDatabase.deleteSpecific(e.getPlayer().getUniqueId());
        }
    }
}
