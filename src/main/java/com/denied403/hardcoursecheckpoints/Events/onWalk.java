package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import com.denied403.hardcoursecheckpoints.Points.PointsManager;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.*;
import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class onWalk implements Listener {
    private final PointsManager pointsManager;
    private final Random random = new Random();
    private static CheckpointDatabase database;

    public onWalk() {
        this.pointsManager = new PointsManager();
    }
    public static void initalize(CheckpointDatabase db) {database = db;}

    @EventHandler
    public void onWalk(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        Location loc = p.getLocation();
        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX && p.getLocation().getBlock().getType() == Material.OAK_SIGN) {

            double checkpointNumber;
            try {
                Sign sign = (Sign) loc.getBlock().getState();
                String line = sign.getLine(0);
                checkpointNumber = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
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

            double previousLevel = database.getLevel(uuid) != null ? database.getLevel(uuid) : 0;

            if (previousLevel < checkpointNumber) {
                if (!p.hasPermission("hardcourse.staff") && checkpointNumber > previousLevel + 1) {
                    if (!plugin.isSkipExempted((int) previousLevel, (int) checkpointNumber)) {
                        Location respawnLocation = p.getRespawnLocation();
                        if (respawnLocation != null) p.teleport(respawnLocation);
                        p.sendMessage(Colorize("&c&lHARDCOURSE &rCheckpoint skipped! You have been returned to your previous checkpoint. Please try to complete all levels."));
                        return;
                    }
                }

                if (checkpointNumber > previousLevel + 10 && !plugin.isSkipExempted((int) previousLevel, (int) checkpointNumber)) {
                    if (isDiscordEnabled()) {
                        sendMessage(p, null, "hacks",
                                Double.toString(previousLevel).replace(".0", ""),
                                Double.toString(checkpointNumber).replace(".0", ""));
                    }
                    Bukkit.broadcast(Colorize("&c&lHARDCOURSE&r &c" + p.getName() + " &fskipped from level &c" +
                            Double.toString(previousLevel).replace(".0", "") + " &fto level &c" +
                            Double.toString(checkpointNumber).replace(".0", "") + "&f!"), "hardcourse.staff");
                }

                p.sendActionBar(Colorize("&fCheckpoint reached: &c" + Double.toString(checkpointNumber).replace(".0", "")));
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

                int pointsToAdd = 10 + random.nextInt(11);
                pointsManager.addPoints(uuid, pointsToAdd);
                sendPointsSubtitle(p, "&a+" + pointsToAdd + " points");

                p.setRespawnLocation(loc.add(0, 1, 0), true);
                database.setCheckpointData(uuid, season, (int) checkpointNumber, database.getPoints(uuid) != null ? database.getPoints(uuid) : 0);

                if (season == 1 && checkpointNumber == 543.0) {
                    if (previousLevel >= 542.0) {
                        handleSeasonComplete(p, 2, "1");
                    } else {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }

                if (season == 2 && checkpointNumber == 365.0) {
                    if (previousLevel >= 363.0) {
                        handleSeasonComplete(p, 3, "2");
                    } else {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
                    }
                }

                if (season == 3 && checkpointNumber == 240.0) {
                    if (previousLevel >= 238.0) {
                        if (isDiscordEnabled()) sendMessage(p, null, "winning", "3", null);
                        p.sendMessage(Colorize("&aCongratulations! You have completed Season 3! There is currently no Season 4, so you have reached the end of the Hardcourse for now."));
                    } else if (!p.hasPermission("hardcourse.staff")) {
                        p.sendMessage(Colorize("&c&lHARDCOURSE &fYou have reached the end. However, we have reason to believe you are &4cheating&f. If you are not, please contact a staff member to verify your progress."));
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
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + p.getName() + " parent add " + nextSeason);
        p.sendMessage(Colorize("&aYou have been teleported to the next season. You can now continue your journey!"));
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
