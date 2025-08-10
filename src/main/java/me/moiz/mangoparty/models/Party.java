package me.moiz.mangoparty.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Party {
    private UUID leader;
    private Set<UUID> members;
    private Map<UUID, Long> invites; // UUID -> expiration time
    private boolean inMatch;
    
    public Party(UUID leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.invites = new HashMap<>();
        this.members.add(leader);
        this.inMatch = false;
    }
    
    public UUID getLeader() {
        return leader;
    }
    
    public void setLeader(UUID leader) {
        this.leader = leader;
    }
    
    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }
    
    public void addMember(UUID member) {
        members.add(member);
        invites.remove(member);
    }
    
    public void removeMember(UUID member) {
        members.remove(member);
    }
    
    public boolean isMember(UUID player) {
        return members.contains(player);
    }
    
    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }
    
    public void addInvite(UUID player, long expirationTime) {
        invites.put(player, expirationTime);
    }
    
    public void removeInvite(UUID player) {
        invites.remove(player);
    }
    
    public boolean hasInvite(UUID player) {
        Long expiration = invites.get(player);
        if (expiration == null) return false;
        
        if (System.currentTimeMillis() > expiration) {
            invites.remove(player);
            return false;
        }
        return true;
    }
    
    public void cleanExpiredInvites() {
        long currentTime = System.currentTimeMillis();
        invites.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
    
    public int getSize() {
        return members.size();
    }
    
    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }
    
    public boolean isInMatch() {
        return inMatch;
    }
    
    public void setInMatch(boolean inMatch) {
        this.inMatch = inMatch;
    }
    
    public void disbandParty() {
        members.clear();
        invites.clear();
    }
}
