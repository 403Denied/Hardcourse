package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import static com.denied403.core403.Util.ColorUtil.Colorize;

public class MiscEvents implements Listener {

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if(!event.getPlayer().hasPermission("hardcourse.staff")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().toLowerCase();
        if (command.equalsIgnoreCase("/suicide") || command.equalsIgnoreCase("/die") || command.equalsIgnoreCase("/stuck")) {
            e.getPlayer().sendMessage(Colorize("<click:run_command:'/clock'><prefix>Hey! Try using your <accent>clock<main> instead. Lost it? Click here, or run <accent>/clock"));
        }
    }

    @EventHandler
    public void onHungerEvent(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void portalEnterEvent(PlayerPortalEvent event) {
        event.setCancelled(true);
    }
}
