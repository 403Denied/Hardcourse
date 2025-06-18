package com.denied403.hardcoursecheckpoints.Discord;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

public class DiscordButtonListener extends ListenerAdapter {
    private final HardcourseCheckpoints plugin;

    public DiscordButtonListener(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (id.startsWith("ban:")) {
            String playerName = id.substring("ban:".length());

            // Optionally acknowledge the button press
            event.reply("Issued ban command for **" + playerName + "**.").setEphemeral(true).queue();

            // Dispatch ban command to server
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + playerName + " Hacking");
            });
        }
    }
}

