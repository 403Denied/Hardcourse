package com.denied403.Hardcourse.Events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;
import static com.transfemme.dev.core403.Commands.Moderation.Vanish.Vanished.vanishedPlayers;
import static com.transfemme.dev.core403.Util.ColorUtil.Colorize;

public class onSneak implements Listener {
    @EventHandler
    public void onSneakEvent(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        if (p.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.JUKEBOX && p.getLocation().getBlock().getType() == Material.OAK_SIGN) {
            double checkpointNumber;
            try {
                Sign sign = (Sign) loc.getBlock().getState();
                String line = sign.getLine(0);
                checkpointNumber = Double.parseDouble(line.replaceAll("[^\\d.]", ""));
            } catch (Exception exception) {
                return;
            }
            String worldName = p.getWorld().getName();
            if (!worldName.startsWith("Season")) return;

            int season;
            try {
                season = Integer.parseInt(worldName.replace("Season", ""));
            } catch (NumberFormatException exception) {
                return;
            }
            int playerSeason = checkpointDatabase.getSeason(p.getUniqueId()) != null ? checkpointDatabase.getSeason(p.getUniqueId()) : 1;
            double level = checkpointDatabase.getLevel(p.getUniqueId());
            if(playerSeason == season && level == checkpointNumber){
                if(vanishedPlayers.contains(p.getUniqueId())){return;}
                p.sendActionBar(Colorize("<main>Reset Orientation: <accent>" + Double.toString(checkpointNumber).replace(".0", "")));
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                p.setRespawnLocation(loc.add(0, 1, 0), true);
            }
        }
    }
}
