package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Match;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaBoundsListener implements Listener {
    private MangoParty plugin;
    private Map<UUID, Long> lastTeleportTime;
    
    public ArenaBoundsListener(MangoParty plugin) {
        this.plugin = plugin;
        this.lastTeleportTime = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null || match.isPlayerSpectator(player.getUniqueId())) {
            return; // Not in match or spectating
        }
        
        Arena arena = match.getArena();
        if (arena.getCorner1() == null || arena.getCorner2() == null) {
            return; // Arena bounds not set
        }
        
        Location playerLoc = player.getLocation();
        
        // Check if player is outside arena bounds
        if (!isWithinBounds(playerLoc, arena)) {
            // Prevent spam teleporting
            long currentTime = System.currentTimeMillis();
            Long lastTeleport = lastTeleportTime.get(player.getUniqueId());
            if (lastTeleport != null && currentTime - lastTeleport < 2000) { // 2 second cooldown
                return;
            }
            
            lastTeleportTime.put(player.getUniqueId(), currentTime);
            
            // Teleport player back to their spawn
            Location spawnLocation;
            if ("split".equalsIgnoreCase(match.getMatchType())) {
                int team = match.getPlayerTeam(player.getUniqueId());
                spawnLocation = team == 1 ? arena.getSpawn1() : arena.getSpawn2();
            } else {
                spawnLocation = arena.getCenter();
            }
            
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.sendMessage("§cYou left the arena bounds! Teleported back to spawn.");
                
                // Schedule removal of cooldown after 5 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        lastTeleportTime.remove(player.getUniqueId());
                    }
                }.runTaskLater(plugin, 100L); // 5 seconds
            }
        }
    }
    
    private boolean isWithinBounds(Location location, Arena arena) {
        Location corner1 = arena.getCorner1();
        Location corner2 = arena.getCorner2();
        
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        return x >= minX && x <= maxX && 
               y >= minY && y <= maxY && 
               z >= minZ && z <= maxZ;
    }
}
