package com.denied403.hardcoursecheckpoints.Points;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Ominous implements Listener, CommandExecutor {

    private final Set<UUID> activeTrails = new HashSet<>();
    private final Map<UUID, Double> rotationAngles = new HashMap<>(); // Track per-player rotation
    private final JavaPlugin plugin;

    public Ominous(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("ominoustrail").setExecutor(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : activeTrails) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        Location loc = player.getLocation().clone();
                        double y = loc.getY() + 0.25;
                        double centerX = loc.getX();
                        double centerZ = loc.getZ();

                        int particles = 10;
                        double radius = 0.5;

                        // Get and update this player's current rotation
                        double rotation = rotationAngles.getOrDefault(uuid, 0.0);
                        rotation += Math.PI / 30; // Increase angle to rotate
                        rotationAngles.put(uuid, rotation);

                        for (int i = 0; i < particles; i++) {
                            // Add rotation to each angle
                            double angle = 2 * Math.PI * i / particles + rotation;
                            double xOffset = radius * Math.cos(angle);
                            double zOffset = radius * Math.sin(angle);

                            Location particleLoc = new Location(
                                    player.getWorld(),
                                    centerX + xOffset,
                                    y,
                                    centerZ + zOffset
                            );

                            double motionX = -xOffset * 0.5;
                            double motionZ = -zOffset * 0.5;

                            player.getWorld().spawnParticle(
                                    Particle.OMINOUS_SPAWNING,
                                    particleLoc,
                                    0,
                                    motionX, 0, motionZ, 0
                            );
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (activeTrails.contains(uuid)) {
            activeTrails.remove(uuid);
            rotationAngles.remove(uuid); // stop tracking rotation
            player.sendMessage(ChatColor.GRAY + "Ominous trail " + ChatColor.RED + "disabled" + ChatColor.GRAY + ".");
        } else {
            activeTrails.add(uuid);
            rotationAngles.put(uuid, 0.0); // initialize rotation
            player.sendMessage(ChatColor.GRAY + "Ominous trail " + ChatColor.DARK_PURPLE + "enabled" + ChatColor.GRAY + "!");
        }
        return true;
    }
}