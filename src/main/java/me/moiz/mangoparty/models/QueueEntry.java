package me.moiz.mangoparty.models;

import org.bukkit.entity.Player;

public class QueueEntry {
    private Player player;
    private String mode;
    private String kitName;
    private long joinTime;
    
    public QueueEntry(Player player, String mode, String kitName) {
        this.player = player;
        this.mode = mode;
        this.kitName = kitName;
        this.joinTime = System.currentTimeMillis();
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public String getMode() { return mode; }
    public String getKitName() { return kitName; }
    public long getJoinTime() { return joinTime; }
}
