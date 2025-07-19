package com.denied403.hardcoursecheckpoints.Commands.Trails;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class OminousTrail implements Listener {

    private static final Set<UUID> activeTrails = new HashSet<>();
    private static final Map<UUID, Double> rotationAngles = new HashMap<>();

    public OminousTrail(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Start particle animation loop
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

                        double rotation = rotationAngles.getOrDefault(uuid, 0.0);
                        rotation += Math.PI / 30;
                        rotationAngles.put(uuid, rotation);

                        for (int i = 0; i < particles; i++) {
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

    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender() instanceof Player)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player player = (Player) sender;
                    UUID uuid = player.getUniqueId();

                    if (activeTrails.contains(uuid)) {
                        activeTrails.remove(uuid);
                        rotationAngles.remove(uuid);
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rOminous trail &cdisabled&r."));
                    } else {
                        activeTrails.add(uuid);
                        rotationAngles.put(uuid, 0.0);
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rOminous trail &cenabled&r!"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
