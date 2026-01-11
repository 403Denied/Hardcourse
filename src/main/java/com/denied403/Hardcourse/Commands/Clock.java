package com.denied403.Hardcourse.Commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.denied403.Hardcourse.Points.Shop.PointsShop.givePointsShopPaper;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class Clock {
    public static LiteralCommandNode<CommandSourceStack> createCommand(final String commandName) {
        return Commands.literal(commandName)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Entity executor = ctx.getSource().getExecutor();
                    if(!(executor instanceof Player player)) {
                        sender.sendMessage(Colorize("<prefix>This command can only be used by players."));
                        return Command.SINGLE_SUCCESS;
                    }
                    player.getInventory().clear();
                    giveItems(player);
                    if(executor == sender){
                        player.sendMessage(Colorize("<prefix>You have been given your items!"));
                        return 1;
                    }
                    sender.sendMessage(Colorize("<prefix>You have given items to <accent>" + player.getName() + "<main>!"));
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
    public static void giveItems(Player player) {
        Material killItem = Material.CLOCK;
        ItemStack killItemStack = new ItemStack(killItem);

        var killItemMeta = killItemStack.getItemMeta();
        killItemMeta.addEnchant(Enchantment.INFINITY, 1, true);
        killItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        killItemMeta.itemName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));
        killItemMeta.displayName(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false));

        var lore = new java.util.ArrayList<Component>();
        lore.add(Colorize(" "));
        lore.add(Colorize("&7Click if you're stuck to go back to your level").decoration(TextDecoration.ITALIC, false));
        killItemMeta.lore(lore);
        killItemStack.setItemMeta(killItemMeta);

        player.getInventory().setItem(8, killItemStack);
        if(isDev) {
            givePointsShopPaper(player, true);

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

            player.getInventory().setItem(0, torch);
        }
    }
}
