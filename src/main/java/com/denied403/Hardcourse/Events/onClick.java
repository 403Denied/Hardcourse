package com.denied403.Hardcourse.Events;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class onClick implements Listener {
    @EventHandler
    public void onClickEvent(PlayerInteractEvent event) {
        if(event.getItem() == null) {
            return;
        } if(event.getItem().getItemMeta().itemName().equals(Colorize("&c&lStuck").decoration(TextDecoration.ITALIC, false)) && event.getItem().getType() == Material.CLOCK){
            event.getPlayer().setHealth(0);
            return;
        } if(event.getItem().getType() == Material.TORCH && event.getItem().getItemMeta().itemName().equals(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false))) {
            if (isDev){
                event.getPlayer().performCommand("hideplayers");
                event.setCancelled(true);
                ItemStack soulTorch = new ItemStack(Material.SOUL_TORCH, 1);
                ItemMeta soulTorchMeta = soulTorch.getItemMeta();
                soulTorchMeta.displayName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
                soulTorchMeta.itemName(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false));
                soulTorch.setItemMeta(soulTorchMeta);
                event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), soulTorch);
                return;
            }
        } if(event.getItem().getType() == Material.SOUL_TORCH && event.getItem().getItemMeta().itemName().equals(Colorize("&cShow &rPlayers").decoration(TextDecoration.ITALIC, false))){
            if(isDev) {
                event.setCancelled(true);
                event.getPlayer().performCommand("hideplayers");
                ItemStack torch = new ItemStack(Material.TORCH, 1);
                ItemMeta torchMeta = torch.getItemMeta();
                torchMeta.displayName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
                torchMeta.itemName(Colorize("&cHide &rPlayers").decoration(TextDecoration.ITALIC, false));
                torch.setItemMeta(torchMeta);
                event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), torch);
            }
        }
    }
}
