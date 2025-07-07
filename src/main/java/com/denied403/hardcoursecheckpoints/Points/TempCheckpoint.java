package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TempCheckpoint implements Listener {

    private boolean isTemporaryCheckpointBook(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return false;

        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Temporary Checkpoint");
    }

    @EventHandler
    public void onPlayerUseTempCheckpoint(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; // Only respond to main hand

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!isTemporaryCheckpointBook(itemInHand)) return;

        // Get block directly beneath the player's feet
        Location belowPlayer = player.getLocation().subtract(0, 1, 0);
        Block blockBelow = belowPlayer.getBlock();

        // Check if block below is solid
        if (blockBelow.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You can't place a temporary checkpoint here!");
            event.setCancelled(true);
            return;
        }

        // Set the respawn location slightly above the block
        Location respawnLocation = blockBelow.getLocation().add(0.5, 1, 0.5);
        Block blockAbove1 = blockBelow.getRelative(BlockFace.UP);
        Block blockAbove2 = blockAbove1.getRelative(BlockFace.UP);
        Block blockAbove3 = blockAbove2.getRelative(BlockFace.UP);

        if (!blockAbove1.isPassable() || !blockAbove2.isPassable() || !blockAbove3.isPassable()) {
            player.sendMessage(ChatColor.RED + "You can't place a temporary checkpoint here!");
            event.setCancelled(true);
            return;
        }
        player.setBedSpawnLocation(respawnLocation, true);


        // Remove one Temporary Checkpoint book from player's main hand
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(itemInHand);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.sendMessage(ChatColor.GREEN + "Temporary checkpoint set!");
        event.setCancelled(true);
    }

}