package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpectatorListener implements Listener {
    private MangoParty plugin;
    
    public SpectatorListener(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        Match match = plugin.getMatchManager().getPlayerMatch(damager);
        
        if (match == null) return;
        
        // Prevent damage during countdown or preparation
        if (match.getState() == Match.MatchState.COUNTDOWN || 
            match.getState() == Match.MatchState.PREPARING) {
            event.setCancelled(true);
            return;
        }
        
        // Prevent spectators from dealing damage
        if (match.isPlayerSpectator(damager.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        // Prevent friendly fire in all team-based matches
        if (match.getState() == Match.MatchState.ACTIVE && 
            event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            
            // Check if both players are in the same match and same team
            if (plugin.getMatchManager().isInMatch(victim)) {
                int damagerTeam = match.getPlayerTeam(damager.getUniqueId());
                int victimTeam = match.getPlayerTeam(victim.getUniqueId());
                
                // Prevent friendly fire in all team-based matches (split, queue modes, party vs party)
                if (damagerTeam == victimTeam && damagerTeam > 0 && 
                    (match.getMatchType().equalsIgnoreCase("split") || 
                     match.getMatchType().startsWith("queue_") || 
                     match.getMatchType().equalsIgnoreCase("partyvs"))) {
                    event.setCancelled(true);
                    return; // No message to avoid spam
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match != null) {
            // Prevent damage during countdown/preparation or if spectating
            if (match.getState() == Match.MatchState.COUNTDOWN || 
                match.getState() == Match.MatchState.PREPARING ||
                match.isPlayerSpectator(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match != null && match.isPlayerSpectator(player.getUniqueId())) {
            // Allow spectators to interact but prevent block changes
            if (event.getClickedBlock() != null) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match != null && match.isPlayerSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) return;
        
        Player player = (Player) event.getTarget();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match != null && match.isPlayerSpectator(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    public void makeSpectator(Player player) {
        // Set to survival mode but make invisible and allow flight
        player.setGameMode(GameMode.SURVIVAL);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Hide from other players
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.hidePlayer(plugin, player);
            }
        }
    }
    
    public void resetSpectator(Player player) {
        // Remove invisibility and flight
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setAllowFlight(false);
        player.setFlying(false);
        
        // Show to other players
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(player) && online.isOnline()) {
                online.showPlayer(plugin, player);
            }
        }
    }
}
