package com.denied403.Hardcourse.Events;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import com.denied403.Hardcourse.Points.PointsManager;
import com.transfemme.dev.core403.Core403;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.jda;
import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.Hardcourse.Hardcourse.*;
import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.denied403.Hardcourse.Utils.Luckperms.addRank;

public class onWalk implements Listener {
    private final PointsManager pointsManager;
    private final Random random = new Random();
    private static CheckpointDatabase database;

    public onWalk() {
        this.pointsManager = new PointsManager();
    }
    public static void initialize(CheckpointDatabase db) {database = db;}

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
            int playerSeason = database.getSeason(uuid) != null ? database.getSeason(uuid) : 1;
            if(playerSeason > season){
                return;
            }
            if(event.getPlayer().getGameMode() == GameMode.SPECTATOR){
                return;
            }

            double previousLevel = database.getLevel(uuid) != null ? database.getLevel(uuid) : 0;

            if (checkpointNumber > previousLevel) {
                if(isDiscordEnabled()) {
                    ThreadChannel channel = jda.getThreadChannelById("1454207642854756362");
                    if (channel != null) {
                        final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
                        f.setTimeZone(TimeZone.getTimeZone("UTC"));
                        if (season == 1) {
                            channel.sendMessage("`[" + f.format(new Date()) + "] " + p.getName() + ": " + String.valueOf(previousLevel).replace(".0", "") + " -> " + String.valueOf(checkpointNumber).replace(".0", "") + "`").queue();
                        } else {
                            channel.sendMessage("`[" + f.format(new Date()) + "] " + p.getName() + ": " + String.valueOf(season).replace(".0", "") + "-" + String.valueOf(previousLevel).replace(".0", "") + " -> " + String.valueOf(season).replace(".0", "") + "-" + String.valueOf(checkpointNumber).replace(".0", "") + "`").queue();
                        }
                    }
                }
                if (checkpointNumber > previousLevel + 10 && !plugin.isSkipExempted((int) previousLevel, (int) checkpointNumber) && !p.hasPermission("hardcourse.staff")) {
                    if (isDiscordEnabled()) {
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
                if(Core403.getVanishedPlayers().contains(p.getUniqueId())) {return;}
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                if(isDev()) {
                    int pointsToAdd = 10 + random.nextInt(11);
                    pointsManager.addPoints(uuid, pointsToAdd);
                    p.sendActionBar(Colorize("&fCheckpoint reached: &c" + Double.toString(checkpointNumber).replace(".0", "") + " &8• &a+" + pointsToAdd + " points"));
                } else {
                    p.sendActionBar(Colorize("&fCheckpoint reached: &c" + Double.toString(checkpointNumber).replace(".0", "")));
                }

                p.setRespawnLocation(loc.add(0, 1, 0), true);
                database.setCheckpointData(uuid, season, checkpointNumber, database.getPoints(uuid) != null ? database.getPoints(uuid) : 0);
                if(!database.checkpointLocationExists(season, checkpointNumber)) {
                    database.storeCheckpointLocationIfAbsent(season, checkpointNumber, loc.subtract(0, 1, 0), difficulty);
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
                        if (isDiscordEnabled()) sendMessage(p, null, "winning", "3", null);
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
        if (isDiscordEnabled()) sendMessage(p, null, "winning", discordSeasonId, null);
        p.sendMessage(Colorize("&aCongratulations! You have completed Season " + discordSeasonId + "!"));
        p.teleport(Bukkit.getWorld("Season" + nextSeason).getSpawnLocation());
        p.setGameMode(GameMode.ADVENTURE);
        p.setRespawnLocation(p.getLocation().add(0, 1, 0), true);
        database.setCheckpointData(p.getUniqueId(), nextSeason, 1, database.getPoints(p.getUniqueId()));
        addRank(p.getUniqueId(), String.valueOf(nextSeason));
        p.sendMessage(Colorize("&aYou have been teleported to the next season. You can now continue your journey!"));
    }
}
