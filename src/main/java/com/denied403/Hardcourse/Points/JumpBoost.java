package com.denied403.Hardcourse.Points;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.ColorUtil.stripAllColors;

public class JumpBoost implements Listener {

    private boolean isJumpBoostBoot(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_BOOTS || !item.hasItemMeta()) return false;
        String name = stripAllColors(item.getItemMeta().displayName());
        return name.equalsIgnoreCase("Jump Boost");
    }

    @EventHandler
    public void onRightClickJumpBoot(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (!isJumpBoostBoot(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item.getAmount() > 0 ? item : null);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 0));
        player.sendMessage(Colorize("&c&lHARDCOURSE &rYou have used jump boost!"));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }
}
