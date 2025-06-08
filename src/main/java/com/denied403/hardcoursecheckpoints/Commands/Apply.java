package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Apply implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player){
            Player p = (Player) commandSender;
            TextComponent apply = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rClick here to apply for our staff team."));
            apply.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forms.gle/waHcpcpurUE1jdnt5"));
            p.spigot().sendMessage(apply);
            return true;
        }
        return false;
    }
}
