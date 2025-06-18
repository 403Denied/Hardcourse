package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.getHighestCheckpoint;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.highestCheckpoint;

public class onWalk implements Listener {
    private final HardcourseCheckpoints plugin;

    public onWalk(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWalk(PlayerMoveEvent event) {
        // Check if the block below the player is a jukebox and the block right above that is a sign
        if (event.getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX &&
                event.getPlayer().getLocation().getBlock().getType() == Material.OAK_SIGN) {

            // Check if the top line of the sign is a number
            Double checkpointNumber;
            try {
                Sign sign = (Sign) event.getPlayer().getLocation().getBlock().getState();
                String line = sign.getLine(0);

                // Remove all non-numeric characters except "."
                String numericLine = line.replaceAll("[^\\d.]", "");

                // Parse the cleaned string to a Double
                checkpointNumber = Double.valueOf(numericLine);
            } catch (NumberFormatException e) {
                return;  // Exit if the cleaned string is not a valid number
            }

            UUID playerUUID = event.getPlayer().getUniqueId();
            Player p = event.getPlayer();
            Double previousCheckpoint = getHighestCheckpoint(playerUUID);

            // Only proceed if the new checkpoint is higher than the current one
            if (previousCheckpoint < checkpointNumber) {
                // Notify staff if the player skipped more than 10 checkpoints
                if (checkpointNumber > previousCheckpoint + 10) {
                    if (!p.hasPermission("hardcourse.staff")) {
                        sendMessage(p, null, "hacks", previousCheckpoint.toString().replace(".0", ""), checkpointNumber.toString().replace(".0", ""));
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (onlinePlayer.hasPermission("hardcourse.staff")) {
                                onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        "&c&lHARDCOURSE &rPlayer &c" + p.getName()
                                                + "&r might be cheating, they skipped from checkpoint &c"
                                                + previousCheckpoint.toString().replace(".0", "")
                                                + " &rto &c" + checkpointNumber.toString().replace(".0", "") + "&r!"));
                            }
                        }
                    }
                }

                // Update the highest checkpoint for the player
                highestCheckpoint.put(playerUUID, checkpointNumber);

                // Notify the player with an action bar
                String message = "§c§lHARDCOURSE §rCheckpoint reached: §c" + checkpointNumber.toString().replace(".0", "") + "§r!";
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                // Set the player's bed spawn location to the current location
                p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                if(checkpointNumber == 543.0) {
                    if (previousCheckpoint >= 540 && p.getWorld().getName().equals("Season1")) {
                        sendMessage(p, null, "winning", null, null);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rCongratulations! You have completed &cSeason 1&f!"));
                        p.teleport(Bukkit.getWorld("Season2").getSpawnLocation());
                        p.setGameMode(GameMode.ADVENTURE);
                        p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                        highestCheckpoint.put(playerUUID, 1.0);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fYou have been teleported to the next season. You can now continue your journey!"));
                    }
                    else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }
                if(checkpointNumber == 350.0) {
                    p.getInventory().getChestplate().setType(Material.AIR);
                }
            }
        }
    }
}
