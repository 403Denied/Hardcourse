package com.denied403.hardcoursecheckpoints.Commands;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import static com.denied403.hardcoursecheckpoints.Utils.ColorUtil.Colorize;

public class RestartForUpdate {

    public static LiteralCommandNode<CommandSourceStack> createCommand(HardcourseCheckpoints plugin, String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    Bukkit.broadcast(Colorize("&c&lHARDCOURSE &fThe server will be restarting to apply updates in &c30 seconds&f. Please find a safe stopping point."));

                    new BukkitRunnable() {
                        int timeLeft = 30;

                        @Override
                        public void run() {
                            if (timeLeft == 15 || (timeLeft <= 5 && timeLeft > 0)) {
                                Bukkit.broadcast(Colorize("&c&lHARDCOURSE &fRestarting in &c" + timeLeft + " second" + (timeLeft == 1 ? "" : "s") + "&f..."));
                            }

                            if (timeLeft == 0) {
                                Bukkit.broadcast(Colorize("&c&lHARDCOURSE &fRestarting now... The server will be back online shortly."));
                                cancel();
                                return;
                            }
                            timeLeft--;
                        }
                    }.runTaskTimer(plugin, 20L, 20L);

                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
