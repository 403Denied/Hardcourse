package com.denied403.hardcoursecheckpoints.Events;

import com.denied403.hardcoursecheckpoints.HardcourseCheckpoints;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.jda;

public class BanListener implements Listener {
    private static HardcourseCheckpoints plugin = JavaPlugin.getPlugin(HardcourseCheckpoints.class);
    public BanListener(HardcourseCheckpoints plugin) {
        BanListener.plugin = plugin;
    }

    public static void runBanCleanup(String playerName) {
        MessageChannel channel = jda.getTextChannelById(plugin.getConfig().getString("Report-Channel-Id"));
        if (channel == null) return;

        channel.getHistory().retrievePast(100).queue(messages -> {
            for (Message msg : messages) {
                List<Button> buttons = msg.getButtons();
                boolean changed = false;

                List<Button> updated = new ArrayList<>();
                for (Button b : buttons) {
                    if (b.getId() != null && b.getId().equalsIgnoreCase("ban:" + playerName)) {
                        updated.add(Button.success(b.getId(), "✅ Banned").asDisabled());
                        changed = true;
                    } else {
                        updated.add(b);
                    }
                }
                if (changed) {
                    channel.editMessageComponentsById(msg.getId(), ActionRow.of(updated)).queue();
                }
            }
        });
    }
}
