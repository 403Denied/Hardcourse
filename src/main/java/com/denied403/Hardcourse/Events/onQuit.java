package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.core403.Core403.playerDatabase;

public class onQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        checkpointDatabase.invalidateCache(e.getPlayer().getUniqueId());
        Double highestLevel = checkpointDatabase.getLevel(e.getPlayer().getUniqueId());
        if(highestLevel == null) return;
        int season = checkpointDatabase.getSeason(e.getPlayer().getUniqueId());
        if(highestLevel <= 3 && season == 1){
            if(!e.getPlayer().isOp() && !e.getPlayer().hasPermission("hardcourse.staff") && !playerDatabase.isLinked(e.getPlayer().getUniqueId())) {
                checkpointDatabase.deleteSpecific(e.getPlayer().getUniqueId());
            }
        }
    }
}
