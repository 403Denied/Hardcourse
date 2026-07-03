package com.denied403.Hardcourse.Events;

import com.denied403.core403.Punishments.Api.ReportEvent;
import com.denied403.core403.Punishments.Api.VanishEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;
import static com.denied403.core403.Util.ColorUtil.Colorize;

public class MiscEvents implements Listener {

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if(!event.getPlayer().hasPermission("hardcourse.staff")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().toLowerCase();
        if (command.equalsIgnoreCase("/suicide") || command.equalsIgnoreCase("/die") || command.equalsIgnoreCase("/stuck")) {
            e.getPlayer().sendMessage(Colorize("<click:run_command:'/clock'><prefix>Hey! Try using your <accent>clock<main> instead. Lost it? Click here, or run <accent>/clock"));
        }
        sendMessage(e.getPlayer(), e.getMessage(), "logs", "command", null);
    }

    @EventHandler
    public void onHungerEvent(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void portalEnterEvent(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onVanishEvent(VanishEvent event){
        Player player = event.getPlayer();
        if(event.getAction().equalsIgnoreCase("vanished")){sendMessage(player, null, "logs", "vanished", (event.isSilent() ? "silent" : ""));}
        else if(event.getAction().equalsIgnoreCase("unvanished")){sendMessage(player, null, "logs", "unvanished", (event.isSilent() ? "silent" : ""));}
    }

    @EventHandler
    public void onReportEvent(ReportEvent event){
        sendMessage(event.getReporter(), event.getReason(), "report", event.getReported().getName(), null);
    }
}
