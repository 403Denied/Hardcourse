package com.denied403.Hardcourse.Events;

import com.transfemme.dev.core403.Punishments.Api.CustomEvents.VanishEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;

public class onVanish implements Listener {
    @EventHandler
    public void onVanishEvent(VanishEvent event){
        Player player = event.getPlayer();
        if(event.getAction().equalsIgnoreCase("vanished")){
            if(!event.isSilent()) {
                sendMessage(player, null, "leave", null, null);
            }
            sendMessage(player, null, "logs", "vanished", (event.isSilent() ? "silent" : ""));
        } else if(event.getAction().equalsIgnoreCase("unvanished")){
            if(!event.isSilent()) {
                sendMessage(player, null, "join", null, null);
            }
            sendMessage(player, null, "logs", "unvanished", (event.isSilent() ? "silent" : ""));
        }
    }

}
