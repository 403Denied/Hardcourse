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

    private HardcourseCheckpoints plugin;

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
    private ItemStack getJumpBoostAllItem() {
        int costPerPlayer = 1500;
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int totalCost = costPerPlayer * onlinePlayers;

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta meta = boots.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b&lBuy everyone jump boost"));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Gives all online players jump boost boots");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + totalCost + " Points");
            meta.setLore(lore);

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            boots.setItemMeta(meta);
        }
        return boots;
    }

    private ItemStack getTempCheckpointItem() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lTemporary Checkpoint"));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Gives you a temporary checkpoint book");
            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + "7500 Points");
            meta.setLore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack getCheckpointBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lTemporary Checkpoint"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Use this book to set a temporary checkpoint.");
            meta.setLore(lore);
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack getCosmeticsItem() {
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lCosmetics"));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Not yet configured");
            meta.setLore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            chest.setItemMeta(meta);
        }
        return chest;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack clickedItem = event.getItem();
        if (isPointsShopPaper(clickedItem)) {
            Player player = event.getPlayer();
            event.setCancelled(true);

            Inventory pointsShopInventory = Bukkit.createInventory(null, 36, "Points Shop");
            pointsShopInventory.setItem(10, getJumpBootsItem());
            pointsShopInventory.setItem(12, getDoubleJumpItem());
            pointsShopInventory.setItem(14, getJumpBoostAllItem());
            pointsShopInventory.setItem(16, getTempCheckpointItem());
            pointsShopInventory.setItem(31, getCosmeticsItem());

            ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setHideTooltip(true);
                filler.setItemMeta(fillerMeta);
            }
            for (int slot = 0; slot < pointsShopInventory.getSize(); slot++) {
                if (pointsShopInventory.getItem(slot) == null) {
                    pointsShopInventory.setItem(slot, filler);
                }

                player.openInventory(pointsShopInventory);
            }
        }
    }

    private final CosmeticsShop cosmeticsShop;

    public PointsShop(HardcourseCheckpoints plugin, CosmeticsShop cosmeticsShop) {
        this.plugin = plugin;
        this.cosmeticsShop = cosmeticsShop;
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
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
            }
        } else if (name.equalsIgnoreCase("Double Jump")) {
            int cost = 2000;
            int currentPoints = pointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getDoubleJumpItem());
                player.sendMessage(ChatColor.GREEN + "You purchased Double Jump!");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
            }
        } else if (name.equalsIgnoreCase("Buy everyone jump boost")) {
            int costPerPlayer = 1500;
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int totalCost = costPerPlayer * onlinePlayers;
            int currentPoints = pointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= totalCost) {
                pointsManager.removePoints(player.getUniqueId(), totalCost);
                ItemStack jumpBoots = getJumpBootsItem();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.getInventory().addItem(jumpBoots.clone());
                    onlinePlayer.sendMessage(ChatColor.AQUA + "You received Jump Boost boots from " + player.getName() + "!");
                }

                String broadcastMsg = ChatColor.RED + "" + ChatColor.BOLD + "HARDCOURSE "
                        + ChatColor.RESET + ChatColor.RED + player.getName()
                        + ChatColor.RESET + " bought "
                        + ChatColor.RESET + "everyone "
                        + ChatColor.RED + "Jump Boost";

                Bukkit.broadcastMessage(broadcastMsg);

                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
            }
        } else if (name.equalsIgnoreCase("Temporary Checkpoint")) {
            int cost = 7500;
            int currentPoints = pointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getCheckpointBook());
                player.sendMessage(ChatColor.GREEN + "You purchased a Temporary Checkpoint book!");
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "You don't have enough points!");
            }
        } else if (name.equalsIgnoreCase("Cosmetics")) {
            player.closeInventory();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cosmeticsShop.openCosmeticsShop(player);
            }, 2L);
        }
    }
    public static void givePointsShopChest(Player player, Boolean inSpot) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lPoints Shop"));
            paper.setItemMeta(meta);
        }
        if(inSpot) {
            player.getInventory().setItem(4, paper);
        } else {
            player.getInventory().addItem(paper);
        }
    }
}