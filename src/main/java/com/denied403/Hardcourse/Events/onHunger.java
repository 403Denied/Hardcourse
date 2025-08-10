package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class onHunger implements Listener {
    @EventHandler
    public void onHunger(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
        }
    }
}
