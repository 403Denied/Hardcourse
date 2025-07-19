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
import org.bukkit.util.Vector;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.denied403.hardcoursecheckpoints.Utils.Colorize.Colorize;

public class EndTrail implements Listener {

    private static final Set<UUID> activeWings = new HashSet<>();

    public EndTrail(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : activeWings) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        spawnWings(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void spawnWings(Player player) {
        // Get player's feet location and raise to approx chest height (~0.9 blocks)
        Location base = player.getLocation().clone().subtract(0, player.getEyeHeight() - 0.9, 0);
        base.setPitch(0); // Ignore pitch to keep horizontal wings

        Vector forward = base.getDirection().setY(0).normalize();
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        // Increased back distance to move wings further behind
        double backDistance = 0.3;
        Vector backOffset = forward.clone().multiply(-backDistance);

        // Slightly lower vertical offset
        double verticalOffset = 1.7;

        Location wingBase = base.clone()
                .add(0, verticalOffset, 0)
                .add(backOffset);

        int segments = 7; // smooth wing curve
        double wingLength = 0.6;
        double wingHeight = 0.4;

        BlockData dustBlock = Material.PINK_CONCRETE_POWDER.createBlockData();

        for (int i = 0; i < segments; i++) {
            double fraction = (double) i / (segments - 1);
            double x = Math.cos(fraction * Math.PI) * wingLength;
            double y = Math.sin(fraction * Math.PI) * wingHeight;

            Location rightWing = wingBase.clone()
                    .add(right.clone().multiply(x))
                    .add(0, y, 0);

            Location leftWing = wingBase.clone()
                    .add(right.clone().multiply(-x))
                    .add(0, y, 0);

            // Slower falling velocity
            player.getWorld().spawnParticle(
                    Particle.FALLING_DUST,
                    rightWing,
                    1,
                    0, 0, 0,
                    -0.01,
                    dustBlock
            );
            player.getWorld().spawnParticle(
                    Particle.FALLING_DUST,
                    leftWing,
                    1,
                    0, 0, 0,
                    -0.01,
                    dustBlock
            );
        }
    }





    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender() instanceof Player)
                .requires(source -> source.getSender().hasPermission("hardcourse.endtrail"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player player = (Player) sender;
                    UUID uuid = player.getUniqueId();

                    if (activeWings.contains(uuid)) {
                        activeWings.remove(uuid);
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rPetal wings &cdisabled&r."));
                    } else {
                        activeWings.add(uuid);
                        player.sendMessage(Colorize("&c&lHARDCOURSE &rPetal wings &cenabled&r!"));
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}