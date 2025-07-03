package com.denied403.hardcoursecheckpoints.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.denied403.hardcoursecheckpoints.Points.PointsShop.givePointsShopChest;

public class Clock implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        } else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rOnly players can use this command?"));
            return false;
        }
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
        givePointsShopChest(player);
        return true;
    }
}
