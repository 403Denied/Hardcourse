package com.denied403.Hardcourse.Events;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;

public class onDeath implements Listener {
    @EventHandler
    public void onDeathEvent(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        sendMessage(player, String.valueOf(player.getStatistic(Statistic.DEATHS)), "logs", "deaths", null);
        if(!player.isOp() || !player.hasPermission("hardcourse.staff")){
            if(player.getInventory().contains(Material.ELYTRA)) {
                player.getInventory().remove(Material.ELYTRA);
            }
        }
    }
}
