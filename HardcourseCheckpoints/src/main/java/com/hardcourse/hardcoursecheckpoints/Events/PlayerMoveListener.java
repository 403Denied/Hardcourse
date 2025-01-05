package com.hardcourse.hardcoursecheckpoints.Events;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import com.hardcourse.hardcoursecheckpoints.Utilities.CheckpointManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    private final HardcourseCheckpoints plugin;
    private final CheckpointManager checkpointManager;

    public PlayerMoveListener(HardcourseCheckpoints plugin, CheckpointManager checkpointManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        World world = location.getWorld();

        if (world == null) return;

        Map<Integer, Location> checkpoints = checkpointManager.getCheckpoints(world);
        UUID playerUUID = player.getUniqueId();

        for (Map.Entry<Integer, Location> entry : checkpoints.entrySet()) {
            if (isSameLocation(entry.getValue(), location)) {
                int checkpointNumber = entry.getKey();
                int highestCheckpoint = checkpointManager.getHighestPlayerCheckpoint(playerUUID, world);

                if (checkpointNumber > highestCheckpoint + 1 && !player.isOp()) {
                    broadcastToOps(player.getName() + " reached checkpoint " + checkpointNumber + " in " + world.getName() + " but their highest checkpoint is " + highestCheckpoint);
                }

                if (!checkpointManager.hasPlayerCheckpoint(playerUUID, world, checkpointNumber)) {
                    checkpointManager.addPlayerCheckpoint(player, world, checkpointNumber);
                    player.sendMessage("You have reached checkpoint " + checkpointNumber + "!");
                    playCheckpointSound(player);
                }
            }
        }
    }


    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }

    private void broadcastToOps(String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isOp()) {
                player.sendMessage(message);
            }
        }
    }

    private void playCheckpointSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

}
