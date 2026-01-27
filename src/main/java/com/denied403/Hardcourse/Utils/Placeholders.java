package com.denied403.Hardcourse.Utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.denied403.Hardcourse.Hardcourse.isDev;
import static com.denied403.Hardcourse.Points.PointsManager.getPoints;
import static com.denied403.Hardcourse.Utils.CheckpointLevelTimer.getCurrentLevelTimeFormatted;
import static com.transfemme.dev.core403.Core403.*;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {return "hardcourse";}
    @Override
    public @NotNull String getAuthor() {return "403Denied";}
    @Override
    public @NotNull String getVersion() {return "1.0.0";}
    @Override
    public boolean persist(){return true;}
    @Override
    public String onRequest(OfflinePlayer player, String params){
        if(params.equalsIgnoreCase("points") && isDev){
            return String.valueOf(getPoints(player.getUniqueId()));
        }
        if(params.equalsIgnoreCase("level")){
            return String.valueOf(checkpointDatabase.getLevel(player.getUniqueId())).replace(".0", "");
        }
        if(params.equalsIgnoreCase("season")){
            return String.valueOf(checkpointDatabase.getSeason(player.getUniqueId()));
        }
        if(params.equalsIgnoreCase("formatted-level")){
            if(checkpointDatabase.getSeason(player.getUniqueId()) == 1){
                return String.valueOf(checkpointDatabase.getLevel(player.getUniqueId())).replace(".0", "");
            } else {
                return checkpointDatabase.getSeason(player.getUniqueId()) + "-" + String.valueOf(checkpointDatabase.getLevel(player.getUniqueId())).replace(".0", "");
            }
        }
        if(params.equalsIgnoreCase("main")){
            return mainColor;
        }
        if(params.equalsIgnoreCase("accent")){
            return accentColor;
        }
        if(params.equalsIgnoreCase("prefix")){
            return prefix;
        }
        if(params.equalsIgnoreCase("level-time")){
            return getCurrentLevelTimeFormatted(player.getUniqueId());
        }
        return null;
    }
}
