package com.denied403.hardcoursecheckpoints.Discord;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import static com.denied403.hardcoursecheckpoints.Events.BanListener.runBanCleanup;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDev;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class DiscordButtonListener extends ListenerAdapter {
    private final HardcourseCheckpoints plugin;

    public DiscordButtonListener(HardcourseCheckpoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(isDiscordEnabled()) {
            String id = event.getComponentId();

            if (id.startsWith("ban:")) {
                String playerName = id.substring("ban:".length());

                event.reply("Issued ban command for **`" + playerName + "`**.").setEphemeral(true).queue();

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "consolepunish " + playerName + " unfair_advantage Discord ban issued by " + event.getUser().getName()));
                runBanCleanup(playerName);
            }
        }
    }
}

