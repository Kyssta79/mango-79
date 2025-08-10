package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
    private MangoParty plugin;
    
    public PlayerDeathListener(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) {
            return; // Player not in a match
        }
        
        // Override death behavior for match players
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Store death location for spectator setup (make it final)
        final Location deathLocation = player.getLocation().clone();
        
        // Handle killer if exists
        Player killer = player.getKiller();
        if (killer != null && plugin.getMatchManager().isInMatch(killer)) {
            match.addKill(killer.getUniqueId());
        }
        
        // Eliminate player from match
        plugin.getMatchManager().eliminatePlayer(player, match);
        
        // Send elimination message
        player.sendTitle("§c§lELIMINATED", "§7You are now spectating", 10, 40, 10);
        
        // Announce elimination to all match players
        for (Player matchPlayer : match.getAllPlayers()) {
            if (!matchPlayer.equals(player)) {
                String killerName = killer != null ? killer.getName() : "unknown causes";
                matchPlayer.sendMessage("§c" + player.getName() + " §7was eliminated by §c" + killerName);
            }
        }
        
        // Schedule spectator setup after respawn (longer delay to ensure respawn is processed)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Teleport back to death location first
                    player.teleport(deathLocation);
                    
                    // Clear inventory completely for spectators
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                    player.updateInventory();
                    
                    // Make player spectator using new system
                    plugin.getSpectatorListener().makeSpectator(player);
                    
                    // Find a living teammate or opponent to spectate
                    Player spectateTarget = null;
                    Match currentMatch = plugin.getMatchManager().getPlayerMatch(player);
                    if (currentMatch != null) {
                        for (Player alive : currentMatch.getAllPlayers()) {
                            if (currentMatch.isPlayerAlive(alive.getUniqueId()) && alive.isOnline()) {
                                spectateTarget = alive;
                                break;
                            }
                        }
                    }
                    
                    if (spectateTarget != null) {
                        final Player finalSpectateTarget = spectateTarget;
                        // Teleport to spectate target after a short delay
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    player.teleport(finalSpectateTarget.getLocation());
                                    player.sendMessage("§7Now spectating §e" + finalSpectateTarget.getName() + "§7. Use §e/spectate <player> §7to switch.");
                                }
                            }
                        }.runTaskLater(plugin, 10L); // 0.5 second delay
                    }
                    
                    // Update scoreboard
                    Match currentMatch2 = plugin.getMatchManager().getPlayerMatch(player);
                    if (currentMatch2 != null) {
                        plugin.getScoreboardManager().updateMatchScoreboards(currentMatch2);
                    }
                }
            }
        }.runTaskLater(plugin, 5L); // Increased delay to 5 ticks
    }
}
