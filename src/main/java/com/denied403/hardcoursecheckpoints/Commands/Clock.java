package com.denied403.hardcoursecheckpoints.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import static com.denied403.hardcoursecheckpoints.Points.PointsShop.givePointsShopChest;
import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class Clock {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName) {
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Entity executor = ctx.getSource().getExecutor();
                    if(!(executor instanceof Player player)) {
                        sender.sendMessage(Colorize("&c&lHARDCOURSE &rThis command can only be used by players."));
                        return Command.SINGLE_SUCCESS;
                    }
                    player.getInventory().clear();
                    giveItems(player);
                    if(executor == sender){
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rYou have been given your items!"));
                        return 1;
                    }
                    sender.sendMessage(Colorize("&c&lHARDCOURSE &rYou have given items to &c" + player.getName() + "&f!"));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
    public static void giveItems(Player player) {
        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);

        var killItemMeta = killItemStack.getItemMeta();
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        killItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        killItemMeta.itemName(Colorize("&c&lStuck"));

        var lore = new java.util.ArrayList<Component>();
        lore.add(Colorize(" "));
        lore.add(Colorize("&7Click if you're stuck to go back to your level"));
        killItemMeta.lore(lore);
        killItemStack.setItemMeta(killItemMeta);

        player.getInventory().setItem(8, killItemStack);
        givePointsShopChest(player, true);
    }
}
