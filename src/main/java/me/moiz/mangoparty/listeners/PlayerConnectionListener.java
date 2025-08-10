package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.models.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private MangoParty plugin;
    
    public PlayerConnectionListener(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove from queue if in one
        plugin.getQueueManager().removePlayer(player.getUniqueId());
        
        // Check if player is in a match
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        if (match != null) {
            // Eliminate player from match
            plugin.getMatchManager().eliminatePlayer(player, match);
            
            // Announce to other players
            for (Player matchPlayer : match.getAllPlayers()) {
                if (!matchPlayer.equals(player) && matchPlayer.isOnline()) {
                    matchPlayer.sendMessage("§c" + player.getName() + " §7left the server and was eliminated!");
                }
            }
        }
        
        // Check if player is in a party
        Party party = plugin.getPartyManager().getParty(player);
        if (party != null) {
            // Remove from party
            plugin.getPartyManager().leaveParty(player);
            
            // Check if party is now empty
            if (party.getSize() == 0) {
                plugin.getPartyManager().disbandParty(party);
            } else {
                // Notify remaining members
                for (Player member : party.getOnlineMembers()) {
                    member.sendMessage("§c" + player.getName() + " §7left the server and was removed from the party!");
                }
            }
        }
    }
}
