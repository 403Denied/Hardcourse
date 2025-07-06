package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.denied403.hardcoursecheckpoints.Points.PointsManager;
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

import java.util.Random;
import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.*;

public class onWalk implements Listener {
    private final HardcourseCheckpoints plugin;
    private final PointsManager pointsManager;
    private final Random random = new Random();

    public onWalk(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
        this.pointsManager = new PointsManager(plugin);
    }

    @EventHandler
    public void onWalk(PlayerMoveEvent event) {
        Player p = event.getPlayer();

        // Check if the block below the player is a jukebox and the block at player's feet is a sign
        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX &&
                p.getLocation().getBlock().getType() == Material.OAK_SIGN) {

            Double checkpointNumber;
            try {
                Sign sign = (Sign) p.getLocation().getBlock().getState();
                String line = sign.getLine(0);

                // Remove all non-numeric characters except "."
                String numericLine = line.replaceAll("[^\\d.]", "");

                // Parse the cleaned string to a Double
                checkpointNumber = Double.valueOf(numericLine);
            } catch (NumberFormatException e) {
                return;  // Exit if not a valid number
            }

            UUID playerUUID = p.getUniqueId();
            Double previousCheckpoint = getHighestCheckpoint(playerUUID);

            // Proceed only if new checkpoint is higher
            if (previousCheckpoint < checkpointNumber) {
                // Notify staff if skipping more than 10 checkpoints
                if (checkpointNumber > previousCheckpoint + 10 && !p.hasPermission("hardcourse.staff")) {
                    if (isDiscordEnabled()) {
                        sendMessage(p, null, "hacks",
                                previousCheckpoint.toString().replace(".0", ""),
                                checkpointNumber.toString().replace(".0", ""));
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("hardcourse.staff")) {
                            onlinePlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&c[Hardcourse] &rPlayer &c" + p.getName()
                                            + "&r might be cheating, they skipped from checkpoint &c"
                                            + previousCheckpoint.toString().replace(".0", "")
                                            + " &rto &c" + checkpointNumber.toString().replace(".0", "") + "&r!"));
                        }
                    }
                }

                // Update highest checkpoint
                highestCheckpoint.put(playerUUID, checkpointNumber);

                // Send checkpoint reached action bar message
                String checkpointMessage = ChatColor.RED + "Checkpoint reached: " + ChatColor.DARK_RED
                        + checkpointNumber.toString().replace(".0", "") + ChatColor.RESET + "!";
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(checkpointMessage));
                p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                // Add random points between 10 and 20
                int pointsToAdd = 10 + random.nextInt(11);
                pointsManager.addPoints(playerUUID, pointsToAdd);

                // Notify player of points gained as a subtitle in the middle of the screen (empty title)
                String pointsMessage = ChatColor.GREEN + "+" + pointsToAdd + " points for completing level "
                        + checkpointNumber.toString().replace(".0", "");
                sendPointsSubtitle(p, pointsMessage);

                // Set respawn location
                p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);

                if (checkpointNumber == 543.0 && p.getWorld().getName().equals("Season1")) {
                    if (previousCheckpoint >= 542.0) {
                        if (isDiscordEnabled()) sendMessage(p, null, "winning", "1", null);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCongratulations! You have completed Season 1!"));
                        p.teleport(Bukkit.getWorld("Season2").getSpawnLocation());
                        p.setGameMode(GameMode.ADVENTURE);
                        p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                        highestCheckpoint.put(playerUUID, 1.0);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have been teleported to the next season. You can now continue your journey!"));
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + p.getName() + " parent add 2");
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have reached the end. However, we have reason to believe you are cheating. If you are not, please contact a staff member to verify your progress."));
                    }
                }

                if (checkpointNumber == 365.0 && p.getWorld().getName().equals("Season2")) {
                    if (previousCheckpoint >= 363.0) {
                        if (isDiscordEnabled()) sendMessage(p, null, "winning", "2", null);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCongratulations! You have completed Season 2!"));
                        p.teleport(Bukkit.getWorld("Season3").getSpawnLocation());
                        p.setGameMode(GameMode.ADVENTURE);
                        p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                        highestCheckpoint.put(playerUUID, 1.0);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have been teleported to the next season. You can now continue your journey!"));
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + p.getName() + " parent add 3");
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have reached the end. However, we have reason to believe you are cheating. If you are not, please contact a staff member to verify your progress."));
                    }
                }

                if (checkpointNumber == 240.0 && p.getWorld().getName().equals("Season3")) {
                    if (previousCheckpoint >= 238.0) {
                        if (isDiscordEnabled()) sendMessage(p, null, "winning", "3", null);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCongratulations! You have completed Season 3! There is currently no Season 4, so you have reached the end of the Hardcourse for now."));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have reached the end. However, we have reason to believe you are cheating. If you are not, please contact a staff member to verify your progress."));
                    }
                }
            }
        }
    }

    // New method: sends the subtitle with empty title, showing the points message for ~3 seconds
    private void sendPointsSubtitle(Player player, String pointsMessage) {
        player.sendTitle("", pointsMessage, 5, 40, 5);  // fadeIn=5, stay=60 (3 sec), fadeOut=5 ticks
    }
}
