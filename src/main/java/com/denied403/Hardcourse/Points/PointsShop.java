package com.denied403.Hardcourse.Points;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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

import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.ColorUtil.stripAllColors;

public class PointsShop implements Listener {

    private boolean isPointsShopPaper(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        Component displayName = item.getItemMeta().displayName();
        return displayName != null && stripAllColors(displayName).equalsIgnoreCase("Points Shop");
    }

    private ItemStack getJumpBootsItem() {
        ItemStack jumpBoots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) jumpBoots.getItemMeta();
        if (meta != null) {
            meta.displayName(Colorize("&c&lJump Boost"));
            meta.itemName(Colorize("&c&lJump Boost"));
            meta.setColor(Color.LIME);

            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("<gray>Gives you 10 seconds of Jump Boost"));
            lore.add(Colorize("&eCost: &61500 Points"));
            meta.lore(lore);

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
            meta.displayName(Colorize("&c&lDouble Jump"));
            meta.itemName(Colorize("&c&lDouble Jump"));

            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("<gray>Acts like a second jump"));
            lore.add(Colorize("<gray>Cost: <gold>2000 Points"));
            meta.lore(lore);

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
            meta.displayName(Colorize("&b&lBuy everyone jump boost"));
            meta.itemName(Colorize("&b&lBuy everyone jump boost"));

            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("<gray>Gives all online players jump boost boots"));
            lore.add(Colorize("<yellow>Cost: <gold>" + totalCost + " Points"));
            meta.lore(lore);

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
            meta.displayName(Colorize("&d&lTemporary Checkpoint"));
            meta.itemName(Colorize("&d&lTemporary Checkpoint"));

            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("<gray>Gives you a temporary checkpoint book"));
            lore.add(Colorize("<yellow>Cost: <gold>7500 Points"));
            meta.lore(lore);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack getCheckpointBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Colorize("&d&lTemporary Checkpoint"));
            meta.itemName(Colorize("&d&lTemporary Checkpoint"));
            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("Use this book to set a temporary checkpoint."));
            meta.lore(lore);
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack getCosmeticsItem() {
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        if (meta != null) {
            meta.displayName(Colorize("&c&lCosmetics"));
            meta.itemName(Colorize("&c&lCosmetics"));

            List<Component> lore = new ArrayList<>();
            lore.add(Colorize("Not yet configured"));
            meta.lore(lore);

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

            Inventory pointsShopInventory = Bukkit.createInventory(null, 36, Colorize("Points Shop"));
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = stripAllColors(event.getView().title());
        if (!title.equalsIgnoreCase("Points Shop")) return;

        if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) return;

        event.setCancelled(true);

        if (event.getClick().isShiftClick() || !event.getClick().isLeftClick()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = stripAllColors(clicked.getItemMeta().displayName());
        PointsManager pointsManager = plugin.getPointsManager();

        if (name.equalsIgnoreCase("Jump Boost")) {
            int cost = 1500;
            int currentPoints = PointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getJumpBootsItem());
                player.sendMessage(Colorize("&c&lHARDCOURSE &rYou purchased Jump Boost boots!"));
                player.closeInventory();
            } else {
                player.sendMessage(Colorize("<red>You don't have enough points!"));
            }
        } else if (name.equalsIgnoreCase("Double Jump")) {
            int cost = 2000;
            int currentPoints = PointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getDoubleJumpItem());
                player.sendMessage(Colorize("&c&lHARDCOURSE &rYou purchased Double Jump!"));
                player.closeInventory();
            } else {
                player.sendMessage(Colorize("<red>You don't have enough points!"));
            }
        } else if (name.equalsIgnoreCase("Buy everyone jump boost")) {
            int costPerPlayer = 1500;
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int totalCost = costPerPlayer * onlinePlayers;
            int currentPoints = PointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= totalCost) {
                pointsManager.removePoints(player.getUniqueId(), totalCost);
                ItemStack jumpBoots = getJumpBootsItem();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.getInventory().addItem(jumpBoots.clone());
                    onlinePlayer.sendMessage(Colorize("&c&lHARDCOURSE &rYou received Jump Boost boots from &c" + player.getName() + "!"));
                }

                Bukkit.broadcast(Colorize("&c&lHARDCOURSE &r&c" + player.getName() + "&r bought everyone &cJump Boost&f!"));

                player.closeInventory();
            } else {
                player.sendMessage(Colorize("&cYou don't have enough points!"));
            }
        } else if (name.equalsIgnoreCase("Temporary Checkpoint")) {
            int cost = 7500;
            int currentPoints = PointsManager.getPoints(player.getUniqueId());

            if (currentPoints >= cost) {
                pointsManager.removePoints(player.getUniqueId(), cost);
                player.getInventory().addItem(getCheckpointBook());
                player.sendMessage(Colorize("&c&lHARDCOURSE &rYou purchased a Temporary Checkpoint book!"));
                player.closeInventory();
            } else {
                player.sendMessage(Colorize("&cYou don't have enough points!"));
            }
        } else if (name.equalsIgnoreCase("Cosmetics")) {
            player.closeInventory();

            Bukkit.getScheduler().runTaskLater(plugin, () -> openCosmeticsShop(player), 2L);
        }
    }
    public static void givePointsShopChest(Player player, Boolean inSpot) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.displayName(Colorize("&c&lPoints Shop").decoration(TextDecoration.ITALIC, false));
            paper.setItemMeta(meta);
        }
        if(inSpot) {
            player.getInventory().setItem(4, paper);
        } else {
            player.getInventory().addItem(paper);
        }
    }

    public void openCosmeticsShop(Player player){
        Inventory gui = Bukkit.createInventory(null, 36, Colorize("Cosmetics Shop"));
        player.openInventory(gui);
    }
}