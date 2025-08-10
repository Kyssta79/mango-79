package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
    private MangoParty plugin;
    private Map<String, Map<String, List<QueueEntry>>> queues; // mode -> kit -> list of players
    private Map<UUID, QueueEntry> playerQueues; // player UUID -> queue entry
    
    public QueueManager(MangoParty plugin) {
        this.plugin = plugin;
        this.queues = new ConcurrentHashMap<>();
        this.playerQueues = new ConcurrentHashMap<>();
        
        // Initialize queue maps for each mode
        queues.put("1v1", new ConcurrentHashMap<>());
        queues.put("2v2", new ConcurrentHashMap<>());
        queues.put("3v3", new ConcurrentHashMap<>());
    }
    
    public void joinQueue(Player player, String mode, String kitName) {
        // Check if player is already in a queue
        if (playerQueues.containsKey(player.getUniqueId())) {
            leaveQueue(player);
        }
        
        // Check if player is in a match
        if (plugin.getMatchManager().isInMatch(player)) {
            player.sendMessage("§cYou cannot join a queue while in a match!");
            return;
        }
        
        // Verify kit exists
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit '" + kitName + "' not found!");
            return;
        }
        
        // Create queue entry
        QueueEntry entry = new QueueEntry(player, mode, kitName);
        
        // Add to queues
        queues.get(mode).computeIfAbsent(kitName, k -> new ArrayList<>()).add(entry);
        playerQueues.put(player.getUniqueId(), entry);
        
        player.sendMessage("§aJoined " + mode + " queue with kit " + kit.getDisplayName());
        
        // Check if we can start a match
        checkForMatch(mode, kitName);
    }
    
    public void leaveQueue(Player player) {
        QueueEntry entry = playerQueues.remove(player.getUniqueId());
        if (entry != null) {
            List<QueueEntry> queueList = queues.get(entry.getMode()).get(entry.getKitName());
            if (queueList != null) {
                queueList.remove(entry);
                if (queueList.isEmpty()) {
                    queues.get(entry.getMode()).remove(entry.getKitName());
                }
            }
            player.sendMessage("§cLeft " + entry.getMode() + " queue");
        } else {
            player.sendMessage("§cYou are not in any queue!");
        }
    }
    
    public void removePlayer(UUID playerId) {
        QueueEntry entry = playerQueues.remove(playerId);
        if (entry != null) {
            List<QueueEntry> queueList = queues.get(entry.getMode()).get(entry.getKitName());
            if (queueList != null) {
                queueList.remove(entry);
                if (queueList.isEmpty()) {
                    queues.get(entry.getMode()).remove(entry.getKitName());
                }
            }
        }
    }
    
    private void checkForMatch(String mode, String kitName) {
        List<QueueEntry> queueList = queues.get(mode).get(kitName);
        if (queueList == null) return;
        
        int requiredPlayers = getRequiredPlayers(mode);
        
        if (queueList.size() >= requiredPlayers) {
            // Get players for the match
            List<QueueEntry> matchEntries = new ArrayList<>();
            for (int i = 0; i < requiredPlayers && i < queueList.size(); i++) {
                matchEntries.add(queueList.get(i));
            }
            
            // Remove players from queue
            for (QueueEntry entry : matchEntries) {
                queueList.remove(entry);
                playerQueues.remove(entry.getPlayer().getUniqueId());
            }
            
            // Clean up empty queue
            if (queueList.isEmpty()) {
                queues.get(mode).remove(kitName);
            }
            
            // Start the match
            startQueueMatch(mode, kitName, matchEntries);
        }
    }
    
    private void startQueueMatch(String mode, String kitName, List<QueueEntry> entries) {
        // Get arena
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            // Return players to queue if no arena available
            for (QueueEntry entry : entries) {
                queues.get(mode).computeIfAbsent(kitName, k -> new ArrayList<>()).add(entry);
                playerQueues.put(entry.getPlayer().getUniqueId(), entry);
                entry.getPlayer().sendMessage("§cNo available arenas! Returned to queue.");
            }
            return;
        }
        
        // Get kit
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            for (QueueEntry entry : entries) {
                entry.getPlayer().sendMessage("§cKit not found! Removed from queue.");
            }
            return;
        }
        
        // Create temporary party for the match
        List<Player> players = new ArrayList<>();
        for (QueueEntry entry : entries) {
            players.add(entry.getPlayer());
        }
        
        // Create a temporary party for match management
        Party tempParty = new Party(players.get(0).getUniqueId());
        for (Player player : players) {
            tempParty.addMember(player.getUniqueId());
        }
        
        // Start the match
        plugin.getMatchManager().startQueueMatch(tempParty, arena, kit, mode, players);
        
        // Notify players
        for (Player player : players) {
            player.sendMessage("§aMatch found! Starting " + mode + " with kit: " + kit.getDisplayName());
        }
    }
    
    private int getRequiredPlayers(String mode) {
        switch (mode) {
            case "1v1": return 2;
            case "2v2": return 4;
            case "3v3": return 6;
            default: return 2;
        }
    }
    
    public int getQueueCount(String mode, String kitName) {
        List<QueueEntry> queueList = queues.get(mode).get(kitName);
        return queueList != null ? queueList.size() : 0;
    }
    
    public boolean isInQueue(Player player) {
        return playerQueues.containsKey(player.getUniqueId());
    }
    
    public QueueEntry getPlayerQueue(Player player) {
        return playerQueues.get(player.getUniqueId());
    }
    
    public void cleanup() {
        queues.clear();
        playerQueues.clear();
    }
}
