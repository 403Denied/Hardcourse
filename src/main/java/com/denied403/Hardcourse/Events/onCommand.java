package com.denied403.Hardcourse.Events;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.commandsChannel;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class onCommand implements Listener {
    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().toLowerCase();
        if (command.equalsIgnoreCase("/suicide") || command.equalsIgnoreCase("/die") || command.equalsIgnoreCase("/stuck")) {
            e.getPlayer().sendMessage(Colorize("<click:run_command:'/clock'>&c&lHARDCOURSE </bold><white>Hey! Try using your &cclock&f instead. Lost it? Click here, or run &c/clock"));
        }
        if(DiscordEnabled) {
            final SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss z");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            commandsChannel.sendMessage("`[" + f.format(new Date()) + "] " + e.getPlayer().getName() + ": " + e.getMessage() + "`").queue();
        }
    }
}
