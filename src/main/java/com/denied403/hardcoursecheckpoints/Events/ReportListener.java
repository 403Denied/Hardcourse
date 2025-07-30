package com.denied403.hardcoursecheckpoints.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.transfemme.dev.core403.Punishments.Api.CustomEvents.ReportEvent;

import static com.denied403.hardcoursecheckpoints.Discord.HardcourseDiscord.reportChannel;
import static com.denied403.hardcoursecheckpoints.HardcourseCheckpoints.isDiscordEnabled;

public class ReportListener implements Listener {
    @EventHandler
    public void onReportEvent(ReportEvent event){
        if(isDiscordEnabled()) {
            reportChannel.sendMessage("`" + event.getReporter().getName() + "` reported `" + event.getReported().getName() + "` for `" + event.getReason() + "`").queue();
        }
    }
}
