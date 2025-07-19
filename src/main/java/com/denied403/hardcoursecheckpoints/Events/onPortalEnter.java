package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class onPortalEnter implements Listener {
    @EventHandler
    public void onPortalEnter(org.bukkit.event.player.PlayerPortalEvent event) {
        event.setCancelled(true);
    }
}
