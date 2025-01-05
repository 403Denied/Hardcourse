package com.hardcourse.hardcoursecheckpoints.Utilities;

import com.hardcourse.hardcoursecheckpoints.HardcourseCheckpoints;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheckpointManager {

    private final HardcourseCheckpoints plugin;
    private final Map<World, FileConfiguration> checkpointConfigs = new HashMap<>();
    private final Map<UUID, Map<World, FileConfiguration>> playerCheckpointConfigs = new HashMap<>();

    public CheckpointManager(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
        loadAllCheckpoints();
    }

    public void addCheckpoint(World world, int checkpointNumber, Location location) {
        FileConfiguration config = getConfig(world);
        String path = "checkpoints." + checkpointNumber;
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        saveConfig(world);
    }

    public void removeCheckpoint(World world, int checkpointNumber) {
        FileConfiguration config = getConfig(world);
        String path = "checkpoints." + checkpointNumber;
        config.set(path, null);
        saveConfig(world);
    }

    public Map<Integer, Location> getCheckpoints(World world){
        FileConfiguration config = getConfig(world);
        Map<Integer, Location> checkpoints = new HashMap<>();
        if (config.contains("checkpoints")) {
            for (String key : config.getConfigurationSection("checkpoints").getKeys(false)) {
                int checkpointNumber = Integer.parseInt(key);
                double x = config.getDouble("checkpoints." + key + ".x");
                double y = config.getDouble("checkpoints." + key + ".y");
                double z = config.getDouble("checkpoints." + key + ".z");
                float yaw = (float) config.getDouble("checkpoints." + key + ".yaw");
                float pitch = (float) config.getDouble("checkpoints." + key + ".pitch");
                Location location = new Location(world, x, y, z, yaw, pitch);
                checkpoints.put(checkpointNumber, location);
            }
        }
        return checkpoints;
    }

    public void addPlayerCheckpoint(Player player, World world, int checkpointNumber) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = getPlayerConfig(playerUUID, world);

        String path = "checkpoints." + checkpointNumber;
        if(!config.contains(path)) {
            config.set(path, true);
            savePlayerConfig(playerUUID, world);
        }

    }

    public boolean hasPlayerCheckpoint(UUID playerUUID, World world, int checkpointNumber) {
        FileConfiguration config = getPlayerConfig(playerUUID, world);
        String path = "checkpoints." + checkpointNumber;
        return config.contains(path);
    }

    public int getHighestPlayerCheckpoint(UUID playerUUID, World world) {
        FileConfiguration config = getPlayerConfig(playerUUID, world);
        int highestCheckpoint = -1;
        if (config.contains("checkpoints")) {
            for (String key : config.getConfigurationSection("checkpoints").getKeys(false)) {
                int checkpointNumber = Integer.parseInt(key);
                if (checkpointNumber > highestCheckpoint) {
                    highestCheckpoint = checkpointNumber;
                }
            }
        }
        return highestCheckpoint;
    }

    public Location getCheckpointLocation(World world, int checkpointNumber){
        FileConfiguration config = getConfig(world);
        String path = "checkpoints." + checkpointNumber;
        if (config.contains(path)) {
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            float yaw = (float) config.getDouble(path + ".yaw");
            float pitch = (float) config.getDouble(path + ".pitch");
            return new Location(world, x, y, z, yaw, pitch);
        }
        return null;
    }

    private FileConfiguration getConfig(World world){
        if(!checkpointConfigs.containsKey(world)){
            File configFile = new File(plugin.getDataFolder(), world.getName() + ".yml");
            createFileIfNotExists(configFile);
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            checkpointConfigs.put(world, config);
        }
        return checkpointConfigs.get(world);
    }

    private void saveConfig(World world){
        File configFile = new File(plugin.getDataFolder(), world.getName() + ".yml");
        try {
            getConfig(world).save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private FileConfiguration getPlayerConfig(UUID playerUUID, World world){
        playerCheckpointConfigs.putIfAbsent(playerUUID, new HashMap<>());
        Map<World, FileConfiguration> playerConfigs = playerCheckpointConfigs.get(playerUUID);
        if(!playerConfigs.containsKey(world)){
            File playerDataFolder = new File(plugin.getDataFolder(), "checkpoints" + File.separator + world.getName());
            createDirectoryIfNotExists(playerDataFolder);
            File configFile = new File(playerDataFolder, playerUUID.toString() + ".yml");
            createFileIfNotExists(configFile);
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            playerConfigs.put(world, config);
        }
        return playerConfigs.get(world);
    }

    private void savePlayerConfig(UUID playerUUID, World world){
        Map<World, FileConfiguration> playerConfigs = playerCheckpointConfigs.get(playerUUID);
        if(playerConfigs != null){
            File playerDataFolder = new File(plugin.getDataFolder(), "checkpoints" + File.separator + world.getName());
            File configFile = new File(playerDataFolder, playerUUID.toString() + ".yml");
            try {
                playerConfigs.get(world).save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAllCheckpoints(){
        for(World world : checkpointConfigs.keySet()){
            saveConfig(world);
        }
        for(UUID playerUUID : playerCheckpointConfigs.keySet()){
            for(World world : playerCheckpointConfigs.get(playerUUID).keySet()){
                savePlayerConfig(playerUUID, world);
            }
        }
    }

    public void loadAllCheckpoints(){
        for (World world : plugin.getServer().getWorlds()){
            getConfig(world);
        }
    }

    private void createFileIfNotExists(File file){
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createDirectoryIfNotExists(File directory){
        if(!directory.exists()){
            directory.mkdirs();
        }
    }

}
