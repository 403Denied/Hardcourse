package com.denied403.Hardcourse.Discord;

import com.denied403.Hardcourse.Hardcourse;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import static com.denied403.Hardcourse.Events.BanListener.runBanCleanup;
import static com.denied403.Hardcourse.Hardcourse.isDiscordEnabled;

public class DiscordButtonListener extends ListenerAdapter {
    private final Hardcourse plugin;

    public DiscordButtonListener(Hardcourse plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(isDiscordEnabled()) {
            String id = event.getComponentId();

            if (id.startsWith("ban:")) {
                String playerName = id.substring("ban:".length());

                event.reply("Issued ban command for **`" + playerName + "`**.").setEphemeral(true).queue();

                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "punish " + playerName + " unfair_advantage Discord ban issued by " + event.getUser().getName()));
                runBanCleanup(playerName);
            }
        }
    }
}

