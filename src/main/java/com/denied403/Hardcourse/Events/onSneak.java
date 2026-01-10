package com.denied403.Hardcourse.Events;

import com.denied403.Hardcourse.Utils.CheckpointDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import static com.denied403.Hardcourse.Utils.ColorUtil.Colorize;
import static com.transfemme.dev.core403.Core403.getVanishedPlayers;

public class onSneak implements Listener {
    private static CheckpointDatabase database;
    public static void initialize(CheckpointDatabase db) {database = db;}

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
            int playerSeason = database.getSeason(p.getUniqueId()) != null ? database.getSeason(p.getUniqueId()) : 1;
            double level = database.getLevel(p.getUniqueId());
            if(playerSeason == season && level == checkpointNumber){
                if(getVanishedPlayers().contains(p.getUniqueId())){return;}
                p.sendActionBar(Colorize("&fReset Orientation: &c" + Double.toString(checkpointNumber).replace(".0", "")));
                p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                p.setRespawnLocation(loc.add(0, 1, 0), true);
            }
        }
    }
}
