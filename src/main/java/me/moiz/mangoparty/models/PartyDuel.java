package me.moiz.mangoparty.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PartyDuel {
    private Player challenger;
    private Player challenged;
    private String kitName;
    private long challengeTime;
    private BukkitTask expirationTask;
    
    public PartyDuel(Player challenger, Player challenged, String kitName) {
        this.challenger = challenger;
        this.challenged = challenged;
        this.kitName = kitName;
        this.challengeTime = System.currentTimeMillis();
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - challengeTime > 60000; // 60 seconds
    }
    
    // Getters and setters
    public Player getChallenger() { return challenger; }
    public Player getChallenged() { return challenged; }
    public String getKitName() { return kitName; }
    public long getChallengeTime() { return challengeTime; }
    public BukkitTask getExpirationTask() { return expirationTask; }
    public void setExpirationTask(BukkitTask task) { this.expirationTask = task; }
}
