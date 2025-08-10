package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Match;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class KitRulesListener implements Listener {
    private MangoParty plugin;
    
    public KitRulesListener(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) return;
        
        Kit kit = match.getKit();
        if (kit != null && !kit.getRules().isNaturalHealthRegen()) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED ||
                event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) return;
        
        Kit kit = match.getKit();
        if (kit != null && !kit.getRules().isBlockBreak()) {
            event.setCancelled(true);
            player.sendMessage("§cBlock breaking is disabled for this kit!");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) return;
        
        Kit kit = match.getKit();
        if (kit != null) {
            if (!kit.getRules().isBlockPlace()) {
                event.setCancelled(true);
                player.sendMessage("§cBlock placing is disabled for this kit!");
                return;
            }
            
            // Handle instant TNT
            if (kit.getRules().isInstantTnt() && event.getBlock().getType().toString().contains("TNT")) {
                event.getBlock().setType(org.bukkit.Material.AIR);
                TNTPrimed tnt = event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
                tnt.setFuseTicks(0); // Instant explosion
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        Match match = plugin.getMatchManager().getPlayerMatch(damager);
        
        if (match == null) return;
        
        Kit kit = match.getKit();
        if (kit != null && kit.getRules().getDamageMultiplier() > 1.0) {
            double newDamage = event.getDamage() * kit.getRules().getDamageMultiplier();
            event.setDamage(newDamage);
        }
    }
}
