package com.denied403.hardcoursecheckpoints.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.gameActive;
import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.getCurrentWord;

public class endChatGame implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (!p.isOp()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYou do not have permission to run that command. If you believe that is an error please contact an administrator."));
                return true;
            }
        }
        if(gameActive){
            gameActive = false;
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rThe chat game has been ended early by an admin. The word was &c" + getCurrentWord()));
            return true;
        }
        else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rThere is no game running currently."));
            return true;
        }
    }
}
