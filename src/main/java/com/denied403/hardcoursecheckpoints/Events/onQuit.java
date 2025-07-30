package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.Utils.CheckpointDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class onQuit implements Listener {
    private static CheckpointDatabase database;

    public static void initialize(CheckpointDatabase db) {
        database = db;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(isDiscordEnabled()) {
            sendMessage(e.getPlayer(), null, "leave", null, null);
        }
        Double highestLevel = database.getLevel(e.getPlayer().getUniqueId());
        if(highestLevel <= 3){
            database.deleteSpecific(e.getPlayer().getUniqueId());
        }
    }
}
