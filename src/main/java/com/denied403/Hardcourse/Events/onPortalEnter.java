package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class onPortalEnter implements Listener {
    @EventHandler
    public void portalEnterEvent(PlayerPortalEvent event) {event.setCancelled(true);}
}
