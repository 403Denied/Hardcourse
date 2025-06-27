package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopItem implements Listener {

    // Gives the player the "Points Shop" paper in hotbar slot 7 (8th slot)
    public void givePointsShopChest(Player player) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lPoints Shop"));
            paper.setItemMeta(meta);
        }

        player.getInventory().setItem(4, paper);
    }

    // Check if an item is the protected Points Shop paper
    private boolean isPointsShopChest(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.PAPER) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String displayName = meta.getDisplayName();
        return displayName != null && displayName.equals(ChatColor.translateAlternateColorCodes('&', "&c&lPoints Shop"));
    }

    // Prevent the paper from being dropped
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();

        if (isPointsShopChest(dropped)) {
            event.setCancelled(true);
        }
    }
}
