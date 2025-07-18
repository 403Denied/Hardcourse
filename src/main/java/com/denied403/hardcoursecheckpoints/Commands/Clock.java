package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class Clock implements CommandExecutor {
    /*@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            if (args.length == 1) {
                String playerName = args[0];
                player = sender.getServer().getPlayerExact(playerName);
                if (player == null) {
                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rPlayer not found"));
                    return true;
                }
            } else {
                sender.sendMessage(Colorize("&c&lHARDCOURSE &rOnly players can use this command or provide a player name"));
                return true;
            }
        }

        player.getInventory().clear();
        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);

        var killItemMeta = killItemStack.getItemMeta();
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        killItemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        killItemMeta.itemName(Colorize("&c&lStuck"));

        var lore = new java.util.ArrayList<String>();
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Click if you're stuck to go back to your level");
        killItemMeta.setLore(lore);
        killItemStack.setItemMeta(killItemMeta);

        player.getInventory().setItem(8, killItemStack);
        givePointsShopChest(player, true);
        return true;
    }*/
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName) {
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Entity executor = ctx.getSource().getExecutor();
                    if(!(executor instanceof Player player)) {
                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be used by players."));
                        return Command.SINGLE_SUCCESS;
                    }
                });
    }
}
