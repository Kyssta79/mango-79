package me.moiz.mangoparty.managers;

import fr.mrmicky.fastboard.FastBoard;
import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
    private MangoParty plugin;
    private Map<UUID, FastBoard> scoreboards;
    private BukkitTask updateTask;
    
    public ScoreboardManager(MangoParty plugin) {
        this.plugin = plugin;
        this.scoreboards = new ConcurrentHashMap<>();
    }
    
    public void startMatchScoreboards(Match match) {
        String title = plugin.getConfig().getString("scoreboard.title", "&#FFD700&lMangoParty");
        title = HexUtils.colorize(title);
        
        // Create scoreboards for all players
        for (Player player : match.getAllPlayers()) {
            FastBoard board = new FastBoard(player);
            board.updateTitle(title);
            scoreboards.put(player.getUniqueId(), board);
        }
        
        // Start update task
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateMatchScoreboards(match);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }
    
    public void updateMatchScoreboards(Match match) {
        List<String> configLines = plugin.getConfig().getStringList("scoreboard.lines");
        
        for (Player player : match.getAllPlayers()) {
            FastBoard board = scoreboards.get(player.getUniqueId());
            if (board == null) continue;
            
            List<String> lines = new ArrayList<>();
            
            for (String line : configLines) {
                String processedLine = processPlaceholders(line, match, player);
                processedLine = HexUtils.colorize(processedLine);
                
                // Skip empty lines and limit to 15 lines max
                if (!processedLine.trim().isEmpty() && lines.size() < 15) {
                    lines.add(processedLine);
                }
            }
            
            board.updateLines(lines);
        }
    }
    
    private String processPlaceholders(String line, Match match, Player player) {
        // Basic match info
        line = line.replace("{arena}", match.getArena().getName());
        line = line.replace("{kit}", match.getKit().getDisplayName());
        line = line.replace("{match_type}", match.getMatchType().toUpperCase());
        line = line.replace("{state}", match.getState().toString());
        
        // Player status
        String status = match.isPlayerAlive(player.getUniqueId()) ? "§aAlive" : "§cSpectating";
        line = line.replace("{status}", status);
        
        // Player counts
        line = line.replace("{players_alive}", String.valueOf(match.getAlivePlayersCount()));
        line = line.replace("{players_total}", String.valueOf(match.getAllPlayersUUIDs().size()));
        line = line.replace("{spectators}", String.valueOf(match.getSpectatorsCount()));
        
        // Player stats
        line = line.replace("{kills}", String.valueOf(match.getPlayerKills(player.getUniqueId())));
        line = line.replace("{deaths}", String.valueOf(match.getPlayerDeaths(player.getUniqueId())));
        
        // Team info (for split matches)
        if ("split".equalsIgnoreCase(match.getMatchType())) {
            int playerTeam = match.getPlayerTeam(player.getUniqueId());
            int opponentTeam = playerTeam == 1 ? 2 : 1;
            
            line = line.replace("{your_team_alive}", String.valueOf(match.getTeamAliveCount(playerTeam)));
            line = line.replace("{your_team_total}", String.valueOf(match.getTeamTotalCount(playerTeam)));
            line = line.replace("{opponent_team_alive}", String.valueOf(match.getTeamAliveCount(opponentTeam)));
            line = line.replace("{opponent_team_total}", String.valueOf(match.getTeamTotalCount(opponentTeam)));
            
            // Individual team counts
            line = line.replace("{team1_alive}", String.valueOf(match.getTeamAliveCount(1)));
            line = line.replace("{team2_alive}", String.valueOf(match.getTeamAliveCount(2)));
        } else {
            // For FFA, team info doesn't apply
            line = line.replace("{your_team_alive}", "N/A");
            line = line.replace("{your_team_total}", "N/A");
            line = line.replace("{opponent_team_alive}", "N/A");
            line = line.replace("{opponent_team_total}", "N/A");
            line = line.replace("{team1_alive}", "N/A");
            line = line.replace("{team2_alive}", "N/A");
        }
        
        // Time
        long duration = match.getMatchDuration();
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        line = line.replace("{time}", timeString);
        
        // Player name
        line = line.replace("{player}", player.getName());
        
        return line;
    }
    
    public void removeScoreboard(Player player) {
        FastBoard board = scoreboards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }
    
    public void cleanup() {
        // Cancel update task
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
        
        // Remove all scoreboards
        for (FastBoard board : scoreboards.values()) {
            board.delete();
        }
        scoreboards.clear();
    }
}
