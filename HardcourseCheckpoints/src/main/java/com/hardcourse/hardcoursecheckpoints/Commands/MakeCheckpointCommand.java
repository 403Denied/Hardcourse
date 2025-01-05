package com.hardcourse.hardcoursecheckpoints.Commands;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import com.hardcourse.hardcoursecheckpoints.Utilities.CheckpointManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class MakeCheckpointCommand implements CommandExecutor {
    private final HardcourseCheckpoints plugin;
    private final CheckpointManager checkpointManager;
    public MakeCheckpointCommand(HardcourseCheckpoints plugin, CheckpointManager checkpointManager) {
        this.plugin = plugin;
        this.checkpointManager = checkpointManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if(!player.hasPermission("hardcourse.admin")){
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if(args.length != 1){
            sender.sendMessage("Usage: /makecheckpoint <number>");
            return false;
        }

        int checkpointNumber;
        try {
            checkpointNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("The number must be an integer.");
            return false;
        }

        Location location = player.getLocation();
        World world = location.getWorld();

        if(world != null){
            checkpointManager.addCheckpoint(world, checkpointNumber, location);
            player.sendMessage("Checkpoint " + checkpointNumber + " set at your current location.");
        } else {
            player.sendMessage("Failed to get the world.");
        }

        return true;
    }
}
