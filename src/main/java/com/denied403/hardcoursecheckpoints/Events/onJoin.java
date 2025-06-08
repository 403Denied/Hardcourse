package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.*;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class onJoin implements Listener {
    @EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        sendMessage(player, null, "join", null);
        player.getInventory().clear();
        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);
        //Set the name of the item to bolded bright red "Stuck" and make it shiny
        org.bukkit.inventory.meta.ItemMeta killItemMeta = killItemStack.getItemMeta();
        //Make the item shiny
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        //Hide the enchantment
        killItemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        killItemMeta.setDisplayName(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + "Stuck");
        //Set the item's lore to "Click if you're stuck to go back to your level"
        java.util.ArrayList<String> lore = new java.util.ArrayList<>();
        lore.add(" ");
        lore.add(org.bukkit.ChatColor.GRAY + "Click if you're stuck to go back to your level");
        killItemMeta.setLore(lore);
        killItemStack.setItemMeta(killItemMeta);
        // Set the inventory slot to the last hotbar slot
        player.getInventory().setItem(8, killItemStack);
        if(!highestCheckpoint.containsKey(player.getUniqueId())) {
            highestCheckpoint.put(player.getUniqueId(), 1.0);
        }
        if(!player.hasPlayedBefore()) {
            World targetWorld = Bukkit.getServer().getWorld("Season1Real");
            Location spawnLocation = targetWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "team join y-default " + player.getDisplayName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rWelcome to hardcourse. We are a parkour server recently brought back after an extended hiatus. This server contains over 500 levels that will test your patience (and your will to live). Think it's worth it? &cYou may begin&r."));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r&c" + player.getDisplayName() + " &rhas joined for the first time. Welcome!"));
        }
        String level = ChatColor.RED + getHighestCheckpoint(player.getUniqueId()).toString().replace(".0", "");
        String formattedTablistName = event.getPlayer().getDisplayName() + net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', " &c" + level);

        event.getPlayer().setPlayerListName(formattedTablistName);


    }
}
