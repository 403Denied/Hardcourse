package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import static com.denied403.hardcoursecheckpoints.Scoreboard.ScoreboardMain.setScoreboard;

public class onWorldChange implements Listener {
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        setScoreboard(e.getPlayer());
    }
}
