package com.denied403.Hardcourse.Events;

import com.transfemme.dev.core403.Punishments.Api.CustomEvents.VanishEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;

public class onVanish implements Listener {
    public static List<Player> vanished = new ArrayList<>();
    @EventHandler
    public void onVanishEvent(VanishEvent event){
        Player player = event.getPlayer();
        if(event.getAction().equalsIgnoreCase("vanished")){sendMessage(player, null, "logs", "vanished", (event.isSilent() ? "silent" : "")); vanished.add(player);}
        else if(event.getAction().equalsIgnoreCase("unvanished")){sendMessage(player, null, "logs", "unvanished", (event.isSilent() ? "silent" : "")); vanished.remove(player);}
    }

}
