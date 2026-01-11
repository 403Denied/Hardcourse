package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.transfemme.dev.core403.Punishments.Api.CustomEvents.ReportEvent;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.reportChannel;
import static com.denied403.Hardcourse.Hardcourse.DiscordEnabled;

public class ReportListener implements Listener {
    @EventHandler
    public void onReportEvent(ReportEvent event){
        if(DiscordEnabled) {
            reportChannel.sendMessage("`" + event.getReporter().getName() + "` reported `" + event.getReported().getName() + "` for `" + event.getReason() + "`").queue();
        }
    }
}
