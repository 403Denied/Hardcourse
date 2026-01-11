package com.denied403.Hardcourse.Chat;

import com.denied403.Hardcourse.Hardcourse;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class ToggleDiabolicalUnscrambles {
    public static LiteralCommandNode<CommandSourceStack> createCommand(String commandName) {
        return Commands.literal(commandName)
                .requires(source -> source.getSender().hasPermission("hardcourse.admin"))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    source.getSender().sendMessage(Colorize("<red><bold>HARDCOURSE <reset>Diabolical Unscrambles are now <red>" + (Hardcourse.DiabolicalUnscrambles ? "disabled" : "enabled") + "<white>."));
                    Hardcourse.DiabolicalUnscrambles = !Hardcourse.DiabolicalUnscrambles;
                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
