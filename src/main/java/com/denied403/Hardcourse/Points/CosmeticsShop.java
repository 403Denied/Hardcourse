package com.denied403.Hardcourse.Points;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;

public class CosmeticsShop implements Listener {
    public void openCosmeticsShop(Player player){
        Inventory gui = Bukkit.createInventory(null, 36, Colorize("Cosmetics Shop"));
        player.openInventory(gui);
    }
}
