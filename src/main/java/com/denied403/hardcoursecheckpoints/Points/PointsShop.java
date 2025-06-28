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
import org.bukkit.inventory.meta.ItemMeta;
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
            lore.add(ChatColor.GRAY + "Gives you 10 seconds of Jump Boost");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + "1500 Points");
            meta.setLore(lore);

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            jumpBoots.setItemMeta(meta);
        }
        return jumpBoots;
    }

    private ItemStack getDoubleJumpItem() {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lDouble Jump"));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Acts like a second jump");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + "2000 Points");
            meta.setLore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            feather.setItemMeta(meta);
        }
        return feather;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack clickedItem = event.getItem();
        if (isPointsShopPaper(clickedItem)) {
            Player player = event.getPlayer();
            event.setCancelled(true);

            Inventory pointsShopInventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Points Shop");
            pointsShopInventory.setItem(10, getJumpBootsItem());
            pointsShopInventory.setItem(12, getDoubleJumpItem());
            player.openInventory(pointsShopInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.equalsIgnoreCase("Points Shop")) return;

        // Prevent taking items from the GUI
        if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) return;

        event.setCancelled(true);

        if (event.getClick().isShiftClick() || !event.getClick().isLeftClick()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Player player = (Player) event.getWhoClicked();
        PointsManager pointsManager = plugin.getPointsManager();

        if (name.equalsIgnoreCase("Jump Boost")) {
            int cost = 1500;
            int currentPoints = pointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getJumpBootsItem());
                player.sendMessage(ChatColor.GREEN + "You purchased Jump Boost boots!");
                pointsManager.sendPointsActionBar(player);
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
                pointsManager.sendTemporaryPointsMessage(player,
                        ChatColor.RED + "Not enough points! Need 1500.",
                        40L);
            }
        }

        if (name.equalsIgnoreCase("Double Jump")) {
            int cost = 2000;
            int currentPoints = pointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getDoubleJumpItem());
                player.sendMessage(ChatColor.GREEN + "You purchased Double Jump!");
                pointsManager.sendPointsActionBar(player);
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
                pointsManager.sendTemporaryPointsMessage(player,
                        ChatColor.RED + "Not enough points! Need 2000.",
                        40L);
            }
        }
    }
}
