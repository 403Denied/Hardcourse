package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class CosmeticsShop implements Listener {
    public void openCosmeticsShop(Player player){
        Inventory gui = Bukkit.createInventory(null, 36, "Cosmetics Shop");
        player.openInventory(gui);
    }
}
