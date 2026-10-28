package com.denied403.Hardcourse.Points;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.denied403.core403.Util.ColorUtil.Colorize;
import static com.denied403.core403.Util.ColorUtil.stripAllColors;

public class JumpBoost implements Listener {

    private boolean isJumpBoostBoot(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_BOOTS || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = stripAllColors(item.getItemMeta().displayName());
        return name.equalsIgnoreCase("Jump Boost");
    }

    @EventHandler
    public void onRightClickJumpBoot(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isJumpBoostBoot(item)) return;

        event.setCancelled(true);

        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItemInMainHand(item.getAmount() > 0 ? item : null);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 0));
        player.sendMessage(Colorize("<prefix>You have used <accent>jump boost<main>!"));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }
}
