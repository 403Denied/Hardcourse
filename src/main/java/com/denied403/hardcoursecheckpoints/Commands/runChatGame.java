package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.Chat.ChatReactions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.denied403.hardcoursecheckpoints.Chat.ChatReactions.gameActive;

public class runChatGame implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player p = (Player) commandSender;
            if (!p.isOp()) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&c&lHARDCOURSE &rYou do not have permission to run that command. If you believe that is an error please contact an administrator."));
                return true;
            }
        }

        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c&lHARDCOURSE &rPlease enter a phrase to start the game."));
            return true;
        }

        // Join all arguments to form the full phrase
        StringBuilder phraseBuilder = new StringBuilder();
        for (String word : strings) {
            phraseBuilder.append(word).append(" ");
        }
        String phrase = phraseBuilder.toString().trim();

        if (phrase.length() < 2) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c&lHARDCOURSE &rThat string is too short. Please try again with a longer term."));
            return true;
        }

        if (!gameActive) {
            ChatReactions.runGame(phrase);
        } else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&c&lHARDCOURSE &rA game is currently running. Please wait."));
        }

        return true;
    }
}
