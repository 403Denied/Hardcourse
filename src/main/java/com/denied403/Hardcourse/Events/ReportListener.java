package com.denied403.Hardcourse.Events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.denied403.core403.Punishments.Api.ReportEvent;

import static com.denied403.Hardcourse.Discord.HardcourseDiscord.sendMessage;

public class ReportListener implements Listener {
    @EventHandler
    public void onReportEvent(ReportEvent event){
        sendMessage(event.getReporter(), event.getReason(), "report", event.getReported().getName(), null);
    }
}
