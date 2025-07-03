package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.setScoreboard;

public class onClick implements Listener {
    @EventHandler
    public void onClick(org.bukkit.event.player.PlayerInteractEvent event) {
        if(event.getItem() == null) {
            return;
        }
        if(event.getItem().getItemMeta().getDisplayName().equals(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "Stuck")){
            event.getPlayer().setHealth(0);
            setScoreboard(event.getPlayer());
        }
        else {
            return;
        }
    }
}
