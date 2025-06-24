package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class onDrop implements Listener {
    @EventHandler
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if(event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "Stuck")){
            event.setCancelled(true);
        }
        if(event.getItemDrop().getItemStack().getType() == Material.ELYTRA){
            event.setCancelled(true);
        }
    }
}
