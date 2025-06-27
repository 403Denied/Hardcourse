package com.denied403.hardcoursecheckpoints.Points;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class PointsShop implements Listener {

    private final HardcourseCheckpoints plugin;

    public PointsShop(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    private boolean isPointsShopPaper(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return displayName != null && ChatColor.stripColor(displayName).equalsIgnoreCase("Points Shop");
    }

    private ItemStack getJumpBootsItem() {
        ItemStack jumpBoots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) jumpBoots.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lJump Boost"));
            meta.setColor(Color.LIME);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Gives you jump boost.");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + "1500 Points");
            meta.setLore(lore);

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            jumpBoots.setItemMeta(meta);
        }
        return jumpBoots;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack clickedItem = event.getItem();
        if (isPointsShopPaper(clickedItem)) {
            Player player = event.getPlayer();
            event.setCancelled(true);

            Inventory pointsShopInventory = Bukkit.createInventory(null, 27, "Points Shop");
            pointsShopInventory.setItem(10, getJumpBootsItem());
            player.openInventory(pointsShopInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!event.getView().getTitle().equals("Points Shop")) return;

        event.setCancelled(true); // Prevent item movement

        // Only allow basic left click (no shift, no right-clicks)
        if (event.getClick().isShiftClick() || !event.getClick().isLeftClick()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (!name.equalsIgnoreCase("Jump Boost")) return;

        int cost = 1500;
        PointsManager pointsManager = plugin.getPointsManager();
        int currentPoints = pointsManager.getPoints(player.getUniqueId());

        // Check if player already has the jump boots equipped
        boolean hasBoots = false;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.isSimilar(getJumpBootsItem())) {
                hasBoots = true;
                break;
            }
        }

        if (hasBoots) {
            player.sendMessage(ChatColor.RED + "You already own the Jump Boost boots.");
            return;
        }

        if (currentPoints >= cost) {
            pointsManager.removePoints(player.getUniqueId(), cost);
            player.getInventory().addItem(getJumpBootsItem());
            player.sendMessage(ChatColor.GREEN + "You purchased Jump Boost boots!");
            pointsManager.sendPointsActionBar(player);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough points!");
            pointsManager.sendTemporaryPointsMessage(player,
                    ChatColor.RED + "Not enough points! Need 1500.",
                    40L);
        }
    }
}
