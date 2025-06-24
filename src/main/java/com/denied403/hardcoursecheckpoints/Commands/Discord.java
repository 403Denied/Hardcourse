package com.denied403.hardcoursecheckpoints.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Discord implements CommandExecutor {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            Component message = miniMessage.deserialize("<red><bold>HARDCOURSE</bold></red> <reset><click:open_url:'https://discord.gg/mh9EBtwnzs'>Click here to join our discord.</click>");
            player.sendMessage(message);
            return true;
        }
        return false;
    }
}
