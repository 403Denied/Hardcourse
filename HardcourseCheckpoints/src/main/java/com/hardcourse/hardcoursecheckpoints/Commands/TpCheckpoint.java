package com.hardcourse.hardcoursecheckpoints.Commands;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import com.hardcourse.hardcoursecheckpoints.Utilities.CheckpointManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
public class TpCheckpoint implements CommandExecutor {
    private final HardcourseCheckpoints plugin;
    private final CheckpointManager checkpointManager;
    public TpCheckpoint(HardcourseCheckpoints plugin, CheckpointManager checkpointManager) {
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
        if(!player.hasPermission("hardcourse.staff")){
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if(strings.length != 1){
            player.sendMessage("Usage: /tpcheckpoint <number>");
            return false;
        }
        int checkpointNumber;
        try {
            checkpointNumber = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("The number must be an integer.");
            return false;
        }
        Location checkpointLocation = checkpointManager.getCheckpointLocation(player.getWorld(), checkpointNumber);
        if(checkpointLocation == null){
            player.sendMessage("Checkpoint " + checkpointNumber + " does not exist.");
            return false;
        }
        player.teleport(checkpointLocation);
        player.sendMessage("Teleported to checkpoint " + checkpointNumber + ".");
        return false;
    }
}
