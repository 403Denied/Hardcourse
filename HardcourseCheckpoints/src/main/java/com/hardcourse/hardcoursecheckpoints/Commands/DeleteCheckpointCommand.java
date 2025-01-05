package com.hardcourse.hardcoursecheckpoints.Commands;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import com.hardcourse.hardcoursecheckpoints.Utilities.CheckpointManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCheckpointCommand implements CommandExecutor {
    private final HardcourseCheckpoints plugin;
    private final CheckpointManager checkpointManager;
    public DeleteCheckpointCommand(HardcourseCheckpoints plugin, CheckpointManager checkpointManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) commandSender;
        if(!player.hasPermission("hardcourse.admin")){
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if(strings.length != 1){
            player.sendMessage("Usage: /deletecheckpoint <number>");
            return false;
        }
        int checkpointNumber;
        try {
            checkpointNumber = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("The number must be an integer.");
            return false;
        }
        player.sendMessage("Checkpoint " + checkpointNumber + " deleted.");
        checkpointManager.removeCheckpoint(player.getWorld(), checkpointNumber);
        return false;
    }
}
