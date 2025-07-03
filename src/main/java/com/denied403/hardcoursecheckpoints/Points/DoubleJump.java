package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DoubleJump implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        // Only process main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.FEATHER || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (!name.equalsIgnoreCase("Double Jump")) return;

        Player player = event.getPlayer();

        // Play cloud particle effect at the player's feet
        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation().add(0, 0.1, 0), // slight Y offset for better visual
                20, // count
                0.3, 0.1, 0.3, // offset x/y/z
                0.01 // speed
        );

        // Simulate a jump boost using upward velocity
        Vector currentVelocity = player.getVelocity();
        player.setVelocity(currentVelocity.setY(0.5)); // adjust jump height as needed

        // Remove ONE feather from the player's hand
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}
