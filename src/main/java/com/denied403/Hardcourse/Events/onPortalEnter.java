package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class onPortalEnter implements Listener {
    @EventHandler
    public void portalEnterEvent(org.bukkit.event.player.PlayerPortalEvent event) {event.setCancelled(true);}
}
