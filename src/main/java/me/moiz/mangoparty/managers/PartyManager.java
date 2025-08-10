package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.models.Party;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager {
    private Map<UUID, Party> playerParties; // Player UUID -> Party
    private Map<UUID, Party> parties; // Leader UUID -> Party
    
    public PartyManager() {
        this.playerParties = new HashMap<>();
        this.parties = new HashMap<>();
    }
    
    public Party createParty(Player leader) {
        if (hasParty(leader)) {
            return null; // Player already in a party
        }
        
        Party party = new Party(leader.getUniqueId());
        parties.put(leader.getUniqueId(), party);
        playerParties.put(leader.getUniqueId(), party);
        
        return party;
    }
    
    public boolean hasParty(Player player) {
        return playerParties.containsKey(player.getUniqueId());
    }
    
    public Party getParty(Player player) {
        return playerParties.get(player.getUniqueId());
    }
    
    public Party getPartyByLeader(UUID leaderUuid) {
        return parties.get(leaderUuid);
    }
    
    public void joinParty(Player player, Party party) {
        if (hasParty(player)) {
            return; // Player already in a party
        }
        
        party.addMember(player.getUniqueId());
        playerParties.put(player.getUniqueId(), party);
    }
    
    public void leaveParty(Player player) {
        Party party = getParty(player);
        if (party == null) return;
        
        party.removeMember(player.getUniqueId());
        playerParties.remove(player.getUniqueId());
        
        // If leader left, transfer leadership or disband
        if (party.isLeader(player.getUniqueId())) {
            if (party.getSize() > 0) {
                // Transfer to first available member
                UUID newLeader = party.getMembers().iterator().next();
                party.setLeader(newLeader);
                parties.remove(player.getUniqueId());
                parties.put(newLeader, party);
            } else {
                // Disband empty party
                disbandParty(party);
            }
        }
    }
    
    public void disbandParty(Party party) {
        // Remove all members from playerParties map
        for (UUID member : party.getMembers()) {
            playerParties.remove(member);
        }
        
        // Remove from parties map
        parties.remove(party.getLeader());
        
        // Clean up party
        party.disbandParty();
    }
    
    public void transferLeadership(Party party, UUID newLeader) {
        if (!party.isMember(newLeader)) {
            return; // New leader must be a member
        }
        
        UUID oldLeader = party.getLeader();
        party.setLeader(newLeader);
        
        // Update parties map
        parties.remove(oldLeader);
        parties.put(newLeader, party);
    }
    
    public void cleanExpiredInvites() {
        for (Party party : parties.values()) {
            party.cleanExpiredInvites();
        }
    }
}
