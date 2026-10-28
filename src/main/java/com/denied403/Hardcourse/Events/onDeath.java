package com.denied403.Hardcourse.Events;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.denied403.Hardcourse.Discord.HardcourseDiscordExtras;

public class onDeath implements Listener {
    @EventHandler
    public void onDeathEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        HardcourseDiscordExtras.logDeath(player);
        if(!player.isOp() && !player.hasPermission("hardcourse.staff")){
            ItemStack[] contents = player.getInventory().getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                if (contents[slot] != null && contents[slot].getType() == Material.ELYTRA) {
                    player.getInventory().setItem(slot, null);
                }
            }
        }
    }
}
