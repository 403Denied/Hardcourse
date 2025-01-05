package com.hardcourse.hardcoursecheckpoints;

import com.hardcourse.hardcoursecheckpoints.Commands.*;
import com.hardcourse.hardcoursecheckpoints.Events.*;
import com.hardcourse.hardcoursecheckpoints.Utilities.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class HardcourseCheckpoints extends JavaPlugin {
    private CheckpointManager checkpointManager;
    @Override
    public void onEnable() {
        checkpointManager = new CheckpointManager(this);
        getCommand("makecheckpoint").setExecutor(new MakeCheckpointCommand(this, checkpointManager));
        getCommand("deletecheckpoint").setExecutor(new DeleteCheckpointCommand(this, checkpointManager));
        getCommand("tpcheckpoint").setExecutor(new TpCheckpoint(this, checkpointManager));
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this, checkpointManager), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, checkpointManager), this);
        getLogger().info("HardcourseCheckpoints enabled!");

    }

    @Override
    public void onDisable() {
        checkpointManager.saveAllCheckpoints();
        getLogger().info("HardcourseCheckpoints disabled!");
    }
    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }
}
