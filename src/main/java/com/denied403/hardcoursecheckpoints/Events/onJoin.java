package com.denied403.hardcoursecheckpoints.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.*;
import static com.denied403.hardcoursecheckpoints.Points.PointsShop.givePointsShopChest;
import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.initScoreboard;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.stripAllColors;

public class onJoin implements Listener {

    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();

        if (isDiscordEnabled()) {
            if (player.hasPlayedBefore()) {
                sendMessage(player, null, "join", null, null);
            } else {
                sendMessage(player, null, "firstjoin", null, null);
            }
        }

        boolean hasPointsShop = false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.PAPER) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && stripAllColors(meta.displayName()).equalsIgnoreCase("Points Shop")) {
                hasPointsShop = true;
                break;
            }
        }
        if (!hasPointsShop) {
            givePointsShopChest(player, true);
        }

        initScoreboard(player);

        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);
        org.bukkit.inventory.meta.ItemMeta killItemMeta = killItemStack.getItemMeta();
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        killItemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        killItemMeta.displayName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));
        killItemMeta.itemName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));
        java.util.ArrayList<Component> stuckLore = new java.util.ArrayList<>();
        stuckLore.add(Colorize(" "));
        stuckLore.add(Colorize("<gray>Click if you're stuck to go back to your level").decoration(TextDecoration.ITALIC, false));
        killItemMeta.lore(stuckLore);
        killItemStack.setItemMeta(killItemMeta);
        if (player.getInventory().contains(killItemStack)) {
            return;
        } else {
            player.getInventory().setItem(8, killItemStack);
        }

        if (!highestCheckpoint.containsKey(player.getUniqueId())) {
            highestCheckpoint.put(player.getUniqueId(), 1.0);
        }

        if (!player.hasPlayedBefore()) {
            World targetWorld = Bukkit.getServer().getWorld("Season1");
            Location spawnLocation = targetWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            player.updateCommands();
            player.sendMessage(Colorize("&c&lHARDCOURSE &rWelcome to hardcourse. This server contains over 1000 levels that will test your patience (and your will to live). Think it's worth it? &cYou may begin&r."));
            Bukkit.broadcast(Colorize("&c&lHARDCOURSE &r&c" + stripAllColors(player.displayName()) + " &rhas joined for the first time. Welcome! &c[#" + Bukkit.getOfflinePlayers().length + "]"));
        }

        ItemStack torch = new ItemStack(Material.TORCH);
        ItemMeta torchMeta = torch.getItemMeta();
        torchMeta.displayName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
        torchMeta.itemName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
        torch.setItemMeta(torchMeta);

        ItemStack soulTorch = new ItemStack(Material.SOUL_TORCH);
        ItemMeta soulTorchMeta = soulTorch.getItemMeta();
        soulTorchMeta.displayName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
        soulTorchMeta.itemName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
        soulTorch.setItemMeta(soulTorchMeta);

        if(player.getInventory().contains(soulTorch)) {
            int index = player.getInventory().first(soulTorch);
            if (index != -1) {
                player.getInventory().setItem(index, torch);
            }
        } if (!player.getInventory().contains(torch) || !player.getInventory().contains(soulTorch)) {
            player.getInventory().setItem(0, torch);
        }
    }
}
