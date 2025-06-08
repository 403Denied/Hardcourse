package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.Utils.PlaytimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Utils.PlaytimeUtil.getPlaytime;

public class Playtime implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0){
            if(commandSender instanceof Player){
                Player p = (Player) commandSender;
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYour playtime is " + getPlaytime(p)));
            }
            else {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rPlease specify a player."));
            }
            return true;
        }
        else if(strings.length == 1){
                OfflinePlayer p = Bukkit.getOfflinePlayer(strings[0]);
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &r&c" + strings[0] + "&r's playtime is " + getPlaytime(p)));
                return true;
            }
        return false;
    }

}
