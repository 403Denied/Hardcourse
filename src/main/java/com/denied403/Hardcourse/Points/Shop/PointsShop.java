package com.denied403.Hardcourse.Points.Shop;

import com.denied403.Hardcourse.Points.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static com.denied403.Hardcourse.Hardcourse.plugin;
import static com.denied403.Hardcourse.Hardcourse.pointsManager;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class PointsShop implements Listener {

    private static final NamespacedKey SHOP_KEY = new NamespacedKey(plugin, "shop_item");

    private static final int SIZE = 36;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String shopTag = meta.getPersistentDataContainer().get(SHOP_KEY, PersistentDataType.STRING);

        if (shopTag == null || !shopTag.equals("POINTS_SHOP")) return;
        event.setCancelled(true);
        openShop(event.getPlayer());
    }


    private void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(
                new PointsShopHolder(), SIZE, Colorize("Points Shop"));

        inv.setItem(10, createJumpBoots());
        inv.setItem(12, createDoubleJump());
        inv.setItem(14, createJumpBoostAll());
        inv.setItem(16, createTempCheckpoint());
        inv.setItem(31, createCosmetics());

        fill(inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof PointsShopHolder)) return;

        event.setCancelled(true);

        if (!event.getClick().isLeftClick()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String id = meta.getPersistentDataContainer().get(SHOP_KEY, PersistentDataType.STRING);

        if (id == null) return;

        ShopItem item = ShopItem.valueOf(id);

        switch (item) {
            case JUMP_BOOTS -> buyJumpBoots(player);
            case DOUBLE_JUMP -> buyDoubleJump(player);
            case JUMP_BOOST_ALL -> buyJumpBoostAll(player);
            case TEMP_CHECKPOINT -> buyCheckpoint(player);
            case COSMETICS -> {
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin,
                        () -> openCosmeticsShop(player), 2L);
            }
        }
    }

    private boolean tryPurchase(Player player, int cost) {
        int points = PointsManager.getPoints(player.getUniqueId());
        if (points < cost) {
            player.sendMessage(Colorize("<prefix>You don't have enough points!"));
            return false;
        }
        pointsManager.removePoints(player.getUniqueId(), cost);
        return true;
    }

    private void buyJumpBoots(Player player) {
        if (!tryPurchase(player, ShopItem.JUMP_BOOTS.cost())) return;
        player.getInventory().addItem(createJumpBoots());
        player.sendMessage(Colorize("<prefix>You purchased <accent>Jump Boost Boots<main>!"));
        player.closeInventory();
    }

    private void buyDoubleJump(Player player) {
        if (!tryPurchase(player, ShopItem.DOUBLE_JUMP.cost())) return;
        player.getInventory().addItem(createDoubleJump());
        player.sendMessage(Colorize("<prefix>You purchased <accent>Double Jump<main>!"));
        player.closeInventory();
    }

    private void buyJumpBoostAll(Player player) {
        int totalCost = ShopItem.JUMP_BOOST_ALL.cost() * Bukkit.getOnlinePlayers().size();
        if (!tryPurchase(player, totalCost)) return;

        ItemStack boots = createJumpBoots();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().addItem(boots.clone());
            p.sendMessage(Colorize("<prefix>You received <accent>Jump Boost Boots<main> from <accent>"
                    + player.getName() + "<main>!"));
        }

        Bukkit.broadcast(Colorize("<prefix><accent>" + player.getName()
                + "<main> bought everyone <accent>Jump Boost Boots<main>!"));

        player.closeInventory();
    }

    private void buyCheckpoint(Player player) {
        if (!tryPurchase(player, ShopItem.TEMP_CHECKPOINT.cost())) return;
        player.getInventory().addItem(createCheckpointBook());
        player.sendMessage(Colorize("<prefix>You purchased a <accent>Temporary Checkpoint Book<main>!"));
        player.closeInventory();
    }

    private ItemStack createJumpBoots() {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        if (!(boots.getItemMeta() instanceof LeatherArmorMeta meta)) return boots;

        meta.displayName(Colorize("&c&lJump Boost"));
        meta.setColor(Color.LIME);
        meta.setUnbreakable(true);
        meta.lore(List.of(
                Colorize("<gray>Gives you 10 seconds of Jump Boost"),
                Colorize("&eCost: &61500 Points")
        ));
        meta.getPersistentDataContainer()
                .set(SHOP_KEY, PersistentDataType.STRING, ShopItem.JUMP_BOOTS.name());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        boots.setItemMeta(meta);
        return boots;
    }

    private ItemStack createDoubleJump() {
        return simpleItem(Material.FEATHER, "&c&lDouble Jump",
                List.of(
                        Colorize("<gray>Acts like a second jump"),
                        Colorize("<gray>Cost: <gold>2000 Points")
                ), ShopItem.DOUBLE_JUMP);
    }

    private ItemStack createJumpBoostAll() {
        int cost = ShopItem.JUMP_BOOST_ALL.cost() * Bukkit.getOnlinePlayers().size();
        return simpleItem(Material.DIAMOND_BOOTS, "&b&lBuy everyone jump boost",
                List.of(
                        Colorize("<gray>Gives all online players jump boost boots"),
                        Colorize("<yellow>Cost: <gold>" + cost + " Points")
                ), ShopItem.JUMP_BOOST_ALL);
    }

    private ItemStack createTempCheckpoint() {
        return simpleItem(Material.BOOK, "&d&lTemporary Checkpoint",
                List.of(
                        Colorize("<gray>Gives you a temporary checkpoint book"),
                        Colorize("<yellow>Cost: <gold>7500 Points")
                ), ShopItem.TEMP_CHECKPOINT);
    }

    private ItemStack createCheckpointBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return book;

        meta.displayName(Colorize("&d&lTemporary Checkpoint"));
        meta.lore(List.of(Colorize("<gray>Use this book to set a temporary checkpoint.")));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack createCosmetics() {
        return simpleItem(Material.CHEST, "&c&lCosmetics",
                List.of(Colorize("<gray>Not yet configured")),
                ShopItem.COSMETICS);
    }

    private ItemStack simpleItem(Material mat, String name, List<Component> lore, ShopItem id) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Colorize(name));
        meta.lore(lore);
        meta.getPersistentDataContainer()
                .set(SHOP_KEY, PersistentDataType.STRING, id.name());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inv) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setHideTooltip(true);
            filler.setItemMeta(meta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }
    }
    public static void givePointsShopPaper(Player player, boolean inSpot) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.displayName(Colorize("&c&lPoints Shop").decoration(TextDecoration.ITALIC, false));

            meta.getPersistentDataContainer().set(
                    PointsShop.SHOP_KEY,
                    PersistentDataType.STRING,
                    "POINTS_SHOP"
            );

            paper.setItemMeta(meta);
        }
        if (inSpot) {
            player.getInventory().setItem(4, paper);
        } else {
            player.getInventory().addItem(paper);
        }
    }



    private void openCosmeticsShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, Colorize("Cosmetics Shop"));
        player.openInventory(inv);
    }
}
