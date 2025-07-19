package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            if (meta != null && meta.hasDisplayName() &&
                    ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Points Shop")) {
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
        killItemMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Stuck");
        java.util.ArrayList<String> stuckLore = new java.util.ArrayList<>();
        stuckLore.add(" ");
        stuckLore.add(ChatColor.GRAY + "Click if you're stuck to go back to your level");
        killItemMeta.setLore(stuckLore);
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rWelcome to hardcourse. This server contains over 1000 levels that will test your patience (and your will to live). Think it's worth it? &cYou may begin&r."));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r&c" + player.getDisplayName() + " &rhas joined for the first time. Welcome! &c[#" + Bukkit.getOfflinePlayers().length + "]"));
        }
    }
}
