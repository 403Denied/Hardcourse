package com.denied403.Hardcourse.Events;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;

public class onDeath implements Listener {
    @EventHandler
    public void onDeathEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        sendMessage(player, String.valueOf(player.getStatistic(Statistic.DEATHS)), "logs", "deaths", null);
        if(!player.isOp() || !player.hasPermission("hardcourse.staff")){
            if(player.getInventory().getItemInOffHand().equals(ItemStack.of(Material.ELYTRA))) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            }
            if(player.getInventory().getChestplate().equals(ItemStack.of(Material.ELYTRA))) {
                player.getInventory().setChestplate(new ItemStack(Material.AIR));
            }
            if(player.getInventory().contains(Material.ELYTRA)) {
                for(ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.ELYTRA) {
                        player.getInventory().remove(item);
                    }
                }
                for (ItemStack item : player.getInventory().getArmorContents()) {
                    if (item != null && item.getType() == Material.ELYTRA) {
                        player.getInventory().remove(item);
                    }
                }
                for (ItemStack item : player.getInventory().getExtraContents()){
                    if (item != null && item.getType() == Material.ELYTRA) {
                        player.getInventory().remove(item);
                    }
                }
                for (ItemStack item : player.getInventory().getStorageContents()){
                    if (item != null && item.getType() == Material.ELYTRA) {
                        player.getInventory().remove(item);
                    }
                }
            }
        }
    }
}
