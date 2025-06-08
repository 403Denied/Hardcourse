package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.Chat.ChatReactions;
import com.denied403.hardcoursecheckpoints.Chat.Shuffler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.gameActive;

public class runChatGame implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (!p.isOp()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rYou do not have permission to run that command. If you believe that is an error please contact an administrator."));
                return true;
            }
        }
        String word;
        String shuffled;
        if (strings.length == 1) {
            if(strings[0].length() < 2){
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rThat string is too short. Please try again with a longer term."));
                return true;
            }
            word = strings[0];
        } else {
            word = ChatReactions.getRandomWord();
        }
        if(!gameActive) {
            ChatReactions.runGame(word);
        }
        else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lHARDCOURSE &rA game is currently running. Please wait."));
            return true;
        }
        return true;
    }
}
