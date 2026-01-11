package com.denied403.Hardcourse.Points;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;
import static com.transfemme.dev.core403.Util.ColorUtil.stripAllColors;

public class TempCheckpoint implements Listener {

    private boolean isTemporaryCheckpointBook(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.displayName() == null) return false;

        return stripAllColors(meta.displayName()).equalsIgnoreCase("Temporary Checkpoint");
    }

    @EventHandler
    public void onPlayerUseTempCheckpoint(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!isTemporaryCheckpointBook(itemInHand)) return;

        Location belowPlayer = player.getLocation().subtract(0, 1, 0);
        Block blockBelow = belowPlayer.getBlock();

        if (blockBelow.getType() == Material.AIR) {
            player.sendMessage(Colorize("&cYou can't place a temporary checkpoint here!"));
            event.setCancelled(true);
            return;
        }

        Location respawnLocation = blockBelow.getLocation().add(0.5, 1, 0.5);
        Block blockAbove1 = blockBelow.getRelative(BlockFace.UP);
        Block blockAbove2 = blockAbove1.getRelative(BlockFace.UP);
        Block blockAbove3 = blockAbove2.getRelative(BlockFace.UP);

        if (!blockAbove1.isPassable() || !blockAbove2.isPassable() || !blockAbove3.isPassable()) {
            player.sendMessage(Colorize("&cYou can't place a temporary checkpoint here!"));
            event.setCancelled(true);
            return;
        }
        player.setRespawnLocation(respawnLocation, true);


        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(itemInHand);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.sendMessage(Colorize("&c&lHARDCOURSE &rTemporary checkpoint set!"));
        event.setCancelled(true);
    }

}