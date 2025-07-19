package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.denied403.hardcoursecheckpoints.Points.PointsManager;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.*;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class onWalk implements Listener {
    private final PointsManager pointsManager;
    private final Random random = new Random();

    public onWalk(HardcourseCheckpoints plugin) {
        this.pointsManager = new PointsManager(plugin);
    }

    @EventHandler
    public void onWalk(PlayerMoveEvent event) {
        Player p = event.getPlayer();

        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX &&
                p.getLocation().getBlock().getType() == Material.OAK_SIGN) {

            Double checkpointNumber;
            try {
                Sign sign = (Sign) p.getLocation().getBlock().getState();
                String line = sign.getLine(0);

                String numericLine = line.replaceAll("[^\\d.]", "");


                checkpointNumber = Double.valueOf(numericLine);
            } catch (NumberFormatException e) {
                return;
            }

            UUID playerUUID = p.getUniqueId();
            Double previousCheckpoint = getHighestCheckpoint(playerUUID);


            if (previousCheckpoint < checkpointNumber) {
                if (!p.hasPermission("hardcourse.staff") && checkpointNumber > previousCheckpoint + 1) {

                    Location respawnLocation = p.getRespawnLocation();
                    if (respawnLocation != null) {
                        p.teleport(respawnLocation);
                    }
                    p.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint skipped! You have been returned to your previous checkpoint. Please try to complete all levels."));

                    if (checkpointNumber > previousCheckpoint + 10) {
                        if (isDiscordEnabled()) {
                            sendMessage(p, null, "hacks",
                                    previousCheckpoint.toString().replace(".0", ""),
                                    checkpointNumber.toString().replace(".0", ""));
                        }
                        Bukkit.broadcast(Colorize("&c&lHARDCOURSE&r &c" + p.getName() + " &fskipped from level &c" +
                                previousCheckpoint.toString().replace(".0", "") + " &fto level &c" +
                                checkpointNumber.toString().replace(".0", "") + "&f!"), "hardcourse.staff");
                    }
                    return;
                }
            highestCheckpoint.put(playerUUID, checkpointNumber);
            p.sendActionBar(Colorize("&fCheckpoint reached: &c" + checkpointNumber.toString().replace(".0", "")));
            p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

            int pointsToAdd = 10 + random.nextInt(11);
            pointsManager.addPoints(playerUUID, pointsToAdd);

            sendPointsSubtitle(p, "&a+" + pointsToAdd + " points");

            p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);

            if (checkpointNumber == 543.0 && p.getWorld().getName().equals("Season1")) {
                if (previousCheckpoint >= 542.0) {
                    if (isDiscordEnabled()) sendMessage(p, null, "winning", "1", null);
                    p.sendMessage(Colorize("&c&lHARDCOURSE &rCongratulations! You have completed Season 1!"));
                    p.teleport(Bukkit.getWorld("Season2").getSpawnLocation());
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                    highestCheckpoint.put(playerUUID, 1.0);
                    p.sendMessage(Colorize("\n&rYou have been teleported to the next season. You can now continue your journey!"));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + p.getName() + " parent add 2");
                } else {
                    p.sendMessage(Colorize("&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                }
            }

            if (checkpointNumber == 365.0 && p.getWorld().getName().equals("Season2")) {
                if (previousCheckpoint >= 363.0) {
                    if (isDiscordEnabled()) sendMessage(p, null, "winning", "2", null);
                    p.sendMessage(Colorize("&aCongratulations! You have completed Season 2!"));
                    p.teleport(Bukkit.getWorld("Season3").getSpawnLocation());
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
                    highestCheckpoint.put(playerUUID, 1.0);
                    p.sendMessage(Colorize("&aYou have been teleported to the next season. You can now continue your journey!"));
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + p.getName() + " parent add 3");
                } else {
                    p.sendMessage(Colorize("&c&lHARDCOURSE &rYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                }
            }

            if (checkpointNumber == 240.0 && p.getWorld().getName().equals("Season3")) {
                if (previousCheckpoint >= 238.0) {
                    if (isDiscordEnabled()) sendMessage(p, null, "winning", "3", null);
                    p.sendMessage(Colorize("&aCongratulations! You have completed Season 3! There is currently no Season 4, so you have reached the end of the Hardcourse for now."));
                } else {
                    if(!p.hasPermission("hardcourse.staff")) {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }
            }
            }
        }
    }

    private void sendPointsSubtitle(Player player, String pointsMessage) {
        Title title = Title.title(
                Colorize(""),
                Colorize(pointsMessage),
                Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(2), Duration.ofMillis(250))
        );
        player.showTitle(title);
    }
}
