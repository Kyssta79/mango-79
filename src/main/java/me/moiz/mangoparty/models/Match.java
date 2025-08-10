package me.moiz.mangoparty.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Match {
    public enum MatchState {
        PREPARING,
        COUNTDOWN,
        ACTIVE,
        ENDING,
        FINISHED
    }
    
    private String id;
    private Party party;
    private Arena arena;
    private Kit kit;
    private String matchType;
    private MatchState state;
    private Map<UUID, Integer> playerTeams;
    private Set<UUID> eliminatedPlayers;
    private Set<UUID> spectators;
    private Map<UUID, Integer> kills;
    private Map<UUID, Integer> deaths;
    private long startTime;
    
    public Match(String id, Party party, Arena arena, Kit kit, String matchType) {
        this.id = id;
        this.party = party;
        this.arena = arena;
        this.kit = kit;
        this.matchType = matchType;
        this.state = MatchState.PREPARING;
        this.playerTeams = new HashMap<>();
        this.eliminatedPlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.kills = new HashMap<>();
        this.deaths = new HashMap<>();
        this.startTime = System.currentTimeMillis();
    }
    
    public void assignTeams() {
        List<Player> players = party.getOnlineMembers();
        Collections.shuffle(players);
        
        for (int i = 0; i < players.size(); i++) {
            int team = (i % 2) + 1; // Alternate between team 1 and 2
            playerTeams.put(players.get(i).getUniqueId(), team);
        }
    }
    
    public void assignPartyVsPartyTeams(Party party1, Party party2) {
        // Assign party1 to team 1
        for (Player player : party1.getOnlineMembers()) {
            playerTeams.put(player.getUniqueId(), 1);
        }
        
        // Assign party2 to team 2
        for (Player player : party2.getOnlineMembers()) {
            playerTeams.put(player.getUniqueId(), 2);
        }
    }
    
    public void eliminatePlayer(UUID playerId) {
        eliminatedPlayers.add(playerId);
        spectators.add(playerId);
        deaths.put(playerId, deaths.getOrDefault(playerId, 0) + 1);
    }
    
    public void addKill(UUID playerId) {
        kills.put(playerId, kills.getOrDefault(playerId, 0) + 1);
    }
    
    public boolean isPlayerEliminated(UUID playerId) {
        return eliminatedPlayers.contains(playerId);
    }
    
    public boolean isPlayerAlive(UUID playerId) {
        return !isPlayerEliminated(playerId);
    }
    
    public void addSpectator(UUID playerId) {
        spectators.add(playerId);
    }
    
    public void removeSpectator(UUID playerId) {
        spectators.remove(playerId);
    }
    
    public boolean isPlayerSpectator(UUID playerId) {
        return spectators.contains(playerId);
    }
    
    public boolean isFinished() {
        if ("ffa".equalsIgnoreCase(matchType)) {
            // FFA ends when only 1 player remains
            return getAlivePlayers().size() <= 1;
        } else if ("split".equalsIgnoreCase(matchType) || matchType.startsWith("queue_") || "partyvs".equalsIgnoreCase(matchType)) {
            // Team matches end when all players of one team are eliminated
            Set<Integer> aliveTeams = new HashSet<>();
            for (Player player : getAllPlayers()) {
                if (!isPlayerEliminated(player.getUniqueId())) {
                    Integer team = playerTeams.get(player.getUniqueId());
                    if (team != null) {
                        aliveTeams.add(team);
                    }
                }
            }
            return aliveTeams.size() <= 1;
        }
        return false;
    }
    
    public UUID getWinner() {
        if ("ffa".equalsIgnoreCase(matchType)) {
            List<Player> alivePlayers = getAlivePlayers();
            return alivePlayers.isEmpty() ? null : alivePlayers.get(0).getUniqueId();
        }
        return null;
    }
    
    public int getWinningTeam() {
        if ("split".equalsIgnoreCase(matchType) || matchType.startsWith("queue_") || "partyvs".equalsIgnoreCase(matchType)) {
            Set<Integer> aliveTeams = new HashSet<>();
            for (Player player : getAllPlayers()) {
                if (!isPlayerEliminated(player.getUniqueId())) {
                    Integer team = playerTeams.get(player.getUniqueId());
                    if (team != null) {
                        aliveTeams.add(team);
                    }
                }
            }
            return aliveTeams.size() == 1 ? aliveTeams.iterator().next() : 0;
        }
        return 0;
    }
    
    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        for (Player player : getAllPlayers()) {
            if (!isPlayerEliminated(player.getUniqueId())) {
                alive.add(player);
            }
        }
        return alive;
    }
    
    public int getAlivePlayersCount() {
        return getAlivePlayers().size();
    }
    
    public int getSpectatorsCount() {
        return spectators.size();
    }
    
    public int getPlayerKills(UUID playerId) {
        return kills.getOrDefault(playerId, 0);
    }
    
    public int getPlayerDeaths(UUID playerId) {
        return deaths.getOrDefault(playerId, 0);
    }
    
    public long getMatchDuration() {
        return System.currentTimeMillis() - startTime;
    }
    
    public List<Player> getAllPlayers() {
        List<Player> allPlayers = new ArrayList<>();
        
        // Add party members
        if (party != null) {
            allPlayers.addAll(party.getOnlineMembers());
        }
        
        // Add any additional players from team assignments (for queue matches)
        for (UUID uuid : playerTeams.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline() && !allPlayers.contains(player)) {
                allPlayers.add(player);
            }
        }
        
        return allPlayers;
    }
    
    public Set<UUID> getAllPlayersUUIDs() {
        Set<UUID> allUUIDs = new HashSet<>();
        
        // Add party members
        if (party != null) {
            for (Player player : party.getOnlineMembers()) {
                allUUIDs.add(player.getUniqueId());
            }
        }
        
        // Add any additional players from team assignments
        allUUIDs.addAll(playerTeams.keySet());
        
        return allUUIDs;
    }
    
    public int getPlayerTeam(UUID playerId) {
        return playerTeams.getOrDefault(playerId, 0);
    }
    
    public List<Player> getTeamPlayers(int team) {
        List<Player> teamPlayers = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : playerTeams.entrySet()) {
            if (entry.getValue() == team) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    teamPlayers.add(player);
                }
            }
        }
        return teamPlayers;
    }
    
    public int getTeamAliveCount(int team) {
        int count = 0;
        for (Map.Entry<UUID, Integer> entry : playerTeams.entrySet()) {
            if (entry.getValue() == team && !isPlayerEliminated(entry.getKey())) {
                count++;
            }
        }
        return count;
    }
    
    public int getTeamTotalCount(int team) {
        int count = 0;
        for (Integer playerTeam : playerTeams.values()) {
            if (playerTeam == team) {
                count++;
            }
        }
        return count;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public Party getParty() { return party; }
    public Arena getArena() { return arena; }
    public Kit getKit() { return kit; }
    public String getMatchType() { return matchType; }
    public MatchState getState() { return state; }
    public void setState(MatchState state) { this.state = state; }
    public Map<UUID, Integer> getPlayerTeams() { return playerTeams; }
    public Set<UUID> getEliminatedPlayers() { return eliminatedPlayers; }
    public Set<UUID> getSpectators() { return spectators; }
    public long getStartTime() { return startTime; }
}
