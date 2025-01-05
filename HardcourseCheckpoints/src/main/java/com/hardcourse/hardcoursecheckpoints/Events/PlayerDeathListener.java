package com.hardcourse.hardcoursecheckpoints.Events;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.hardcourse.hardcoursecheckpoints.Utilities.CheckpointManager;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.World;
import org.bukkit.Location;

import java.util.UUID;

public class PlayerDeathListener implements Listener {
    private final HardcourseCheckpoints plugin;
    private final CheckpointManager checkpointManager;

    public PlayerDeathListener(HardcourseCheckpoints plugin, CheckpointManager checkpointManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        UUID playerUUID = player.getUniqueId();
        Bukkit.broadcastMessage("Loser fuckin died");

        int highestCheckpoint = checkpointManager.getHighestPlayerCheckpoint(playerUUID, world);
        Bukkit.broadcastMessage("Level " + highestCheckpoint);
        if (highestCheckpoint != -1) {
            Bukkit.broadcastMessage("Good :3");
            Location checkpointLocation = checkpointManager.getCheckpointLocation(world, highestCheckpoint);
            if (checkpointLocation != null) {
                Bukkit.broadcastMessage("Good 2");
                event.setRespawnLocation(checkpointLocation);
                player.sendMessage("You have been teleported to checkpoint " + highestCheckpoint);
            }
            else {
                Bukkit.broadcastMessage("Bad!");
            }
        }

    }

}
