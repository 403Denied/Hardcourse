package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Apply implements CommandExecutor {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player player){
            Component message = miniMessage.deserialize("<red><bold>HARDCOURSE<reset> <click:open_url:'https://forms.gle/waHcpcpurUE1jdnt5'>Click here to apply for our staff team.</click>");
            player.sendMessage(message);
            return true;
        }
        return false;
    }
}
