package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class GamemodeChange implements Listener {
    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if(!event.getPlayer().hasPermission("hardcourse.staff")){
            event.setCancelled(true);
        }
    }
}
