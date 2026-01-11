package com.denied403.Hardcourse.Events;

import com.denied403.Hardcourse.Points.PointsManager;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.*;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.Luckperms.addRank;
import static com.transfemme.dev.core403.Commands.Moderation.Vanish.Vanished.vanishedPlayers;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class onWalk implements Listener {
    private final PointsManager pointsManager;
    private final Random random = new Random();

    public onWalk() {
        this.pointsManager = new PointsManager();
    }

    @EventHandler
    public void onWalkEvent(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        Location loc = p.getLocation();
        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX && p.getLocation().getBlock().getType() == Material.OAK_SIGN) {

            double checkpointNumber;
            String difficulty;

            try {
                Sign sign = (Sign) loc.getBlock().getState();
                checkpointNumber = Double.parseDouble(sign.getLine(0).replaceAll("[^\\d.]", ""));
                difficulty = String.join(" ", sign.getLine(1), sign.getLine(2), sign.getLine(3)).trim();
            } catch (Exception e) {
                return;
            }

            UUID uuid = p.getUniqueId();
            String worldName = p.getWorld().getName();

            if (!worldName.startsWith("Season")) return;

            int season;
            try {
                season = Integer.parseInt(worldName.replace("Season", ""));
            } catch (NumberFormatException e) {
                return;
            }
            int playerSeason = checkpointDatabase.getSeason(uuid) != null ? checkpointDatabase.getSeason(uuid) : 1;
            if(playerSeason > season){
                return;
            }
            if(event.getPlayer().getGameMode() == GameMode.SPECTATOR){
                return;
            }

            double previousLevel = checkpointDatabase.getLevel(uuid) != null ? checkpointDatabase.getLevel(uuid) : 0;

            if (checkpointNumber > previousLevel) {
                if(DiscordEnabled) {
                    final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (season == 1) {
                        checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + p.getName() + ": " + String.valueOf(previousLevel).replace(".0", "") + " -> " + String.valueOf(checkpointNumber).replace(".0", "") + "`").queue();
                    } else {
                        checkpointsChannel.sendMessage("`[" + f.format(new Date()) + "] " + p.getName() + ": " + String.valueOf(season).replace(".0", "") + "-" + String.valueOf(previousLevel).replace(".0", "") + " -> " + String.valueOf(season).replace(".0", "") + "-" + String.valueOf(checkpointNumber).replace(".0", "") + "`").queue();
                    }
                }
                if (checkpointNumber > previousLevel + 10 && !plugin.isSkipExempted((int) previousLevel, (int) checkpointNumber) && !p.hasPermission("hardcourse.staff")) {
                    if (DiscordEnabled) {
                        if(playerSeason > 1) {
                            sendMessage(p, null, "hacks",
                                    playerSeason + "-" + Double.toString(previousLevel).replace(".0", ""),
                                    playerSeason + "-" + Double.toString(checkpointNumber).replace(".0", ""));
                        }
                        else {
                            sendMessage(p, null, "hacks",
                                    Double.toString(previousLevel).replace(".0", ""),
                                    Double.toString(checkpointNumber).replace(".0", ""));
                        }
                    }
                    if(playerSeason > 1) {
                        Bukkit.broadcast(Colorize("&c&lHARDCOURSE&r &c" + p.getName() + " &fskipped from level &c" + playerSeason + "-" +
                                Double.toString(previousLevel).replace(".0", "") + " &fto level &c" + playerSeason + "-" +
                                Double.toString(checkpointNumber).replace(".0", "") + "&f!"), "hardcourse.staff");
                    } else {
                        Bukkit.broadcast(Colorize("&c&lHARDCOURSE&r &c" + p.getName() + " &fskipped from level &c" +
                                Double.toString(previousLevel).replace(".0", "") + " &fto level &c" +
                                Double.toString(checkpointNumber).replace(".0", "") + "&f!"), "hardcourse.staff");
                    }
                }
                if (!p.hasPermission("hardcourse.staff") && checkpointNumber > previousLevel + 1) {
                    if (!plugin.isSkipExempted((int) previousLevel, (int) checkpointNumber)) {
                        Location respawnLocation = p.getRespawnLocation();
                        if (respawnLocation != null){ p.teleport(respawnLocation);} else {p.teleport(p.getWorld().getSpawnLocation());}
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint skipped! You have been returned to your previous checkpoint. Please try to complete all levels."));
                        return;
                    }
                }

                Block blockAbove1 = p.getLocation().getBlock();
                Block blockAbove2 = blockAbove1.getRelative(BlockFace.UP);
                Block blockAbove3 = blockAbove2.getRelative(BlockFace.UP);

                if (!blockAbove1.isPassable() || !blockAbove2.isPassable() || !blockAbove3.isPassable()) {
                    p.sendMessage(Colorize("&c&lHARDCOURSE&r You can't set a checkpoint here! Please refrain from making any more progress, and contact an administrator to fix the issue!"));
                    return;
                }
                if(vanishedPlayers.contains(p.getUniqueId())) {return;}
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                if(isDev) {
                    int pointsToAdd = 10 + random.nextInt(11);
                    pointsManager.addPoints(uuid, pointsToAdd);
                    p.sendActionBar(Colorize("&fCheckpoint reached: &c" + Double.toString(checkpointNumber).replace(".0", "") + " &8• &a+" + pointsToAdd + " points"));
                } else {
                    p.sendActionBar(Colorize("&fCheckpoint reached: &c" + Double.toString(checkpointNumber).replace(".0", "")));
                }

                p.setRespawnLocation(loc.add(0, 1, 0), true);
                checkpointDatabase.setCheckpointData(uuid, season, checkpointNumber, checkpointDatabase.getPoints(uuid) != null ? checkpointDatabase.getPoints(uuid) : 0);
                if(!checkpointDatabase.checkpointLocationExists(season, checkpointNumber)) {
                    checkpointDatabase.storeCheckpointLocationIfAbsent(season, checkpointNumber, loc.subtract(0, 1, 0), difficulty);
                }
                if (season == 1 && checkpointNumber == 543.0) {
                    if (previousLevel >= 542.0) {
                        handleSeasonComplete(p, 2, "1");
                    } else {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }
                if (season == 2 && checkpointNumber == 365.0) {
                    if (previousLevel >= 363.0) {
                        handleSeasonComplete(p, 3, "2");
                    } else {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }
                if (season == 3 && checkpointNumber == 240.0) {
                    if (previousLevel >= 238.0) {
                        if(p.getStatistic(Statistic.PLAY_ONE_MINUTE) < 60 && ! p.hasPermission("hardcourse.staff")) {
                            p.sendMessage(Colorize("&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                            return;
                        }
                        if (DiscordEnabled) sendMessage(p, null, "winning", "3", null);
                        p.sendMessage(Colorize("&aCongratulations! You have completed Season 3! There is currently no Season 4, so you have reached the end of the Hardcourse for now."));
                        addRank(p.getUniqueId(), "winner");
                    } else if (!p.hasPermission("hardcourse.staff")) {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }
            }
        }
    }
    private void handleSeasonComplete(Player p, int nextSeason, String discordSeasonId) {
        if (DiscordEnabled) sendMessage(p, null, "winning", discordSeasonId, null);
        p.sendMessage(Colorize("&aCongratulations! You have completed Season " + discordSeasonId + "!"));
        p.teleport(Bukkit.getWorld("Season" + nextSeason).getSpawnLocation());
        p.setGameMode(GameMode.ADVENTURE);
        p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
        checkpointDatabase.setCheckpointData(p.getUniqueId(), nextSeason, 1, checkpointDatabase.getPoints(p.getUniqueId()));
        addRank(p.getUniqueId(), String.valueOf(nextSeason));
        p.sendMessage(Colorize("&aYou have been teleported to the next season. You can now continue your journey!"));
    }
}
