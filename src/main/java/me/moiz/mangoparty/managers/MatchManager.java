package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import me.moiz.mangoparty.models.Match;
import java.util.concurrent.ConcurrentHashMap;

public class MatchManager {
    private MangoParty plugin;
    private Map<String, Match> activeMatches;
    private Map<UUID, String> playerMatches; // Player UUID -> Match ID
    private Map<String, BukkitTask> countdownTasks; // Match ID -> Task
    
    public MatchManager(MangoParty plugin) {
        this.plugin = plugin;
        this.activeMatches = new ConcurrentHashMap<>();
        this.playerMatches = new ConcurrentHashMap<>();
        this.countdownTasks = new HashMap<>();
    }

    public Match getPlayerMatch(Player player) {
        String matchId = playerMatches.get(player.getUniqueId());
        return matchId != null ? activeMatches.get(matchId) : null;
    }

    public void eliminatePlayer(Player player, Match match) {
        match.eliminatePlayer(player.getUniqueId());
        
        // Check if match is finished
        if (match.isFinished()) {
            endMatch(match);
        }
    }

    private String generateMatchId() {
        return "match_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    public void startMatch(Party party, Arena arena, Kit kit, String matchType) {
        if (party.isInMatch()) {
            return; // Party already in match
        }
        
        List<Player> players = party.getOnlineMembers();
        if (players.isEmpty()) {
            return;
        }
        
        // Reserve the arena
        plugin.getArenaManager().reserveArena(arena.getName());
        
        // Create match object
        String matchId = generateMatchId();
        Match match = new Match(matchId, party, arena, kit, matchType);
        
        // Assign teams if split mode
        if ("split".equalsIgnoreCase(matchType)) {
            match.assignTeams();
        }
        
        // Store match
        activeMatches.put(matchId, match);
        for (Player player : players) {
            playerMatches.put(player.getUniqueId(), matchId);
        }
        
        // Set gamerule for immediate respawn and keep it true
        arena.getCenter().getWorld().setGameRuleValue("doImmediateRespawn", "true");
        
        // Regenerate arena
        plugin.getArenaManager().pasteSchematic(arena);
        
        // Set party as in match
        party.setInMatch(true);
        match.setState(Match.MatchState.PREPARING);
        
        // Heal and feed all players
        for (Player player : players) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
        
        // Teleport players based on match type
        if ("split".equalsIgnoreCase(matchType)) {
            startSplitMatch(players, arena, match);
        } else if ("ffa".equalsIgnoreCase(matchType)) {
            startFFAMatch(players, arena);
        }
        
        // Start countdown
        startCountdown(match);
    }
    
    private void startSplitMatch(List<Player> players, Arena arena, Match match) {
        // Teleport teams to their spawns based on match team assignments
        for (Player player : players) {
            int team = match.getPlayerTeam(player.getUniqueId());
            if (team == 1) {
                player.teleport(arena.getSpawn1());
            } else if (team == 2) {
                player.teleport(arena.getSpawn2());
            }
        }
    }
    
    private void startFFAMatch(List<Player> players, Arena arena) {
        // Teleport all players to center
        for (Player player : players) {
            player.teleport(arena.getCenter());
        }
    }

    public void startPartyVsPartyMatch(Match match, Party party1, Party party2) {
        List<Player> allPlayers = match.getAllPlayers();
        if (allPlayers.isEmpty()) {
            return;
        }
        
        Arena arena = match.getArena();
        
        // Store match
        activeMatches.put(match.getId(), match);
        for (Player player : allPlayers) {
            playerMatches.put(player.getUniqueId(), match.getId());
        }
        
        // Set gamerule for immediate respawn
        arena.getCenter().getWorld().setGameRuleValue("doImmediateRespawn", "true");
        
        // Regenerate arena
        plugin.getArenaManager().pasteSchematic(arena);
        
        match.setState(Match.MatchState.PREPARING);
        
        // Heal and feed all players
        for (Player player : allPlayers) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
        
        // Teleport teams to their spawns
        for (Player player : allPlayers) {
            int team = match.getPlayerTeam(player.getUniqueId());
            if (team == 1) {
                player.teleport(arena.getSpawn1());
            } else if (team == 2) {
                player.teleport(arena.getSpawn2());
            }
        }
        
        // Start countdown
        startCountdown(match);
    }

    public void startQueueMatch(Party matchParty, Arena arena, Kit kit, String mode, List<Player> players) {
        // Reserve the arena
        plugin.getArenaManager().reserveArena(arena.getName());
        
        // Create match object
        String matchId = "queue_" + mode + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        Match match = new Match(matchId, matchParty, arena, kit, "queue_" + mode);
        
        // Assign teams based on mode
        assignQueueTeams(match, players, mode);
        
        // Store match
        activeMatches.put(matchId, match);
        for (Player player : players) {
            playerMatches.put(player.getUniqueId(), matchId);
        }
        
        // Set gamerule for immediate respawn
        arena.getCenter().getWorld().setGameRuleValue("doImmediateRespawn", "true");
        
        // Regenerate arena
        plugin.getArenaManager().pasteSchematic(arena);
        
        match.setState(Match.MatchState.PREPARING);
        
        // Heal and feed all players
        for (Player player : players) {
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
        
        // Teleport players based on teams
        for (Player player : players) {
            int team = match.getPlayerTeam(player.getUniqueId());
            if (team == 1) {
                player.teleport(arena.getSpawn1());
            } else if (team == 2) {
                player.teleport(arena.getSpawn2());
            }
        }
        
        // Start countdown
        startCountdown(match);
    }

    private void assignQueueTeams(Match match, List<Player> players, String mode) {
        Collections.shuffle(players); // Randomize teams
        
        int playersPerTeam = getPlayersPerTeam(mode);
        
        for (int i = 0; i < players.size(); i++) {
            int team = (i < playersPerTeam) ? 1 : 2;
            match.getPlayerTeams().put(players.get(i).getUniqueId(), team);
        }
    }

    private int getPlayersPerTeam(String mode) {
        switch (mode) {
            case "1v1": return 1;
            case "2v2": return 2;
            case "3v3": return 3;
            default: return 1;
        }
    }
    
    private void startCountdown(Match match) {
        List<Player> players = match.getAllPlayers();
        match.setState(Match.MatchState.COUNTDOWN);
        
        // Give kits BEFORE countdown starts
        for (Player player : players) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setWalkSpeed(0f);
            player.setFlySpeed(0f);
            
            // Give kit immediately
            plugin.getKitManager().giveKit(player, match.getKit());
        }
        
        // Start scoreboards
        plugin.getScoreboardManager().startMatchScoreboards(match);
        
        BukkitTask countdownTask = new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    // Display countdown
                    for (Player player : players) {
                        if (player.isOnline()) {
                            player.sendTitle("§c" + countdown, "§7Match starting...", 0, 20, 0);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
                        }
                    }
                    countdown--;
                } else {
                    // Start match
                    match.setState(Match.MatchState.ACTIVE);
                    for (Player player : players) {
                        if (player.isOnline()) {
                            // Clear title and show GO message briefly
                            player.sendTitle("§aGO!", "§7Fight!", 0, 20, 10);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                            player.setGameMode(GameMode.SURVIVAL);
                            player.setWalkSpeed(0.2f);
                            player.setFlySpeed(0.1f);
                            
                            // Clear title after 1 second
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline()) {
                                    player.sendTitle("", "", 0, 0, 0);
                                }
                            }, 20L);
                        }
                    }
                    
                    // Update scoreboards
                    plugin.getScoreboardManager().updateMatchScoreboards(match);
                    
                    this.cancel();
                    countdownTasks.remove(match.getId());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        countdownTasks.put(match.getId(), countdownTask);
    }
    
    public void endMatch(Match match) {
        match.setState(Match.MatchState.ENDING);
        
        List<Player> players = match.getAllPlayers();
        
        // Announce winner
        if ("ffa".equalsIgnoreCase(match.getMatchType())) {
            UUID winner = match.getWinner();
            if (winner != null) {
                Player winnerPlayer = Bukkit.getPlayer(winner);
                String winnerName = winnerPlayer != null ? winnerPlayer.getName() : "Unknown";
                
                for (Player player : players) {
                    player.sendTitle("§6§lMATCH ENDED", "§aWinner: " + winnerName, 10, 60, 10);
                }
            }
        } else if ("split".equalsIgnoreCase(match.getMatchType())) {
            int winningTeam = match.getWinningTeam();
            if (winningTeam > 0) {
                for (Player player : players) {
                    player.sendTitle("§6§lMATCH ENDED", "§aTeam " + winningTeam + " Wins!", 10, 60, 10);
                }
            }
        }
        
        // Release the arena
        plugin.getArenaManager().releaseArena(match.getArena().getName());
        
        // Teleport all players to spawn after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : players) {
                playerMatches.remove(player.getUniqueId());
                
                // Reset spectator if they were spectating
                if (match.isPlayerSpectator(player.getUniqueId())) {
                    plugin.getSpectatorListener().resetSpectator(player);
                }
                
                // Reset player state
                player.setGameMode(GameMode.SURVIVAL);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.getInventory().clear();
                
                // Teleport to spawn
                if (plugin.getSpawnLocation() != null) {
                    player.teleport(plugin.getSpawnLocation());
                }
                
                // Remove scoreboard
                plugin.getScoreboardManager().removeScoreboard(player);
                
                // Clear any remaining titles
                player.sendTitle("", "", 0, 0, 0);
            }
        }, 60L); // 3 seconds delay
        
        // Set party as not in match
        match.getParty().setInMatch(false);
        match.setState(Match.MatchState.FINISHED);
        
        // Regenerate arena
        plugin.getArenaManager().pasteSchematic(match.getArena());
        
        // Remove match
        activeMatches.remove(match.getId());
        
        // Cancel countdown task if exists
        BukkitTask task = countdownTasks.remove(match.getId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    
    public boolean isInMatch(Player player) {
        return playerMatches.containsKey(player.getUniqueId());
    }
    
    public void cleanup() {
        // Cancel all countdown tasks
        for (BukkitTask task : countdownTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        countdownTasks.clear();
        
        // Release all reserved arenas
        for (Match match : activeMatches.values()) {
            plugin.getArenaManager().releaseArena(match.getArena().getName());
        }
        
        // Reset all players in match
        for (String matchId : activeMatches.keySet()) {
            Match match = activeMatches.get(matchId);
            for (UUID uuid : match.getAllPlayersUUIDs()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    // Reset spectator state
                    plugin.getSpectatorListener().resetSpectator(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setWalkSpeed(0.2f);
                    player.setFlySpeed(0.1f);
                }
            }
        }
        activeMatches.clear();
        playerMatches.clear();
    }
}
