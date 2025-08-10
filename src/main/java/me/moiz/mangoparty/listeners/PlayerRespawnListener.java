package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private MangoParty plugin;
    
    public PlayerRespawnListener(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) {
            return; // Player not in a match
        }
        
        // For match players, we don't cancel the event but we set a custom respawn location
        // If player is eliminated, they should respawn at their death location
        if (match.isPlayerSpectator(player.getUniqueId())) {
            // Set respawn location to their current location (death location)
            // This prevents them from being teleported to world spawn
            event.setRespawnLocation(player.getLocation());
        }
    }
}
