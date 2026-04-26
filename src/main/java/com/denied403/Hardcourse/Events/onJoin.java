package com.denied403.Hardcourse.Events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;

import static com.denied403.Hardcourse.Commands.Clock.giveItems;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Points.Shop.PointsShop.givePointsShopPaper;
import static com.denied403.core403.Util.ColorUtil.Colorize;
import static com.denied403.core403.Util.ColorUtil.stripAllColors;

public class onJoin implements Listener {
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.hasPlayedBefore() && !player.hasMetadata("vanished")) {
            sendMessage(player, null, "join", null, null);
        } if(!player.hasPlayedBefore()) {
            sendMessage(player, null, "firstJoin", null, null);
        }
        sendMessage(player, null, "logs", "join", null);

        if(checkpointDatabase.getCheckpointData(player.getUniqueId()) == null) {
            checkpointDatabase.setCheckpointData(player.getUniqueId(), 1, 0);
            player.teleport(player.getWorld().getSpawnLocation());
            player.setRespawnLocation(player.getLocation());
        }
        if(checkpointDatabase.getSeason(player.getUniqueId()) == 0 || checkpointDatabase.getSeason(player.getUniqueId()) == null) {
            checkpointDatabase.setSeason(player.getUniqueId(), 1);
        }
        if(isDev) {
            boolean hasPointsShop = false;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != Material.PAPER) continue;

                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && stripAllColors(meta.displayName()).equalsIgnoreCase("Points Shop")) {
                    hasPointsShop = true;
                    break;
                }
            }
            if (!hasPointsShop) {
                givePointsShopPaper(player, true);
            }
        }
        checkpointUpdating.applyPendingUpdates(player);
        if (!player.hasPlayedBefore()) {
            World targetWorld = Bukkit.getServer().getWorld("Season1");
            assert targetWorld != null;
            Location spawnLocation = targetWorld.getSpawnLocation();
            player.teleport(spawnLocation);
            player.updateCommands();
            checkpointDatabase.setCheckpointData(player.getUniqueId(), 1, 0);
            giveItems(player);
            player.sendMessage(Colorize("<prefix>Welcome to hardcourse. This parkour server contains over 1000 levels that will test your patience (and your will to live). To die you may click the <accent>clock<main> in your 9th hotbar slot. Think it's worth it? <accent>You may begin<main>."));
        }
    }
}
