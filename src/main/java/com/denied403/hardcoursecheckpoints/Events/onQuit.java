package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.setScoreboard;

public class onQuit implements Listener {
    @EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            setScoreboard(p);
        }
    }
}
