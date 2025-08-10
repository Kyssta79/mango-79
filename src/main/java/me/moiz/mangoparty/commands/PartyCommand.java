package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Party;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {
    private MangoParty plugin;
    
    public PartyCommand(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateCommand(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                handleInviteCommand(player, args[1]);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party join <leader>");
                    return true;
                }
                handleJoinCommand(player, args[1]);
                break;
            case "transfer":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party transfer <player>");
                    return true;
                }
                handleTransferCommand(player, args[1]);
                break;
            case "leave":
                handleLeaveCommand(player);
                break;
            case "disband":
                handleDisbandCommand(player);
                break;
            case "match":
            case "fight":
                handleMatchCommand(player);
                break;
            case "info":
                handleInfoCommand(player);
                break;
            case "challenge":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party challenge <leader>");
                    return true;
                }
                handleChallengeCommand(player, args[1]);
                break;
            case "acceptduel":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party acceptduel <challenger>");
                    return true;
                }
                handleAcceptDuelCommand(player, args[1]);
                break;
            case "declineduel":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party declineduel <challenger>");
                    return true;
                }
                handleDeclineDuelCommand(player, args[1]);
                break;
            case "acceptchallenge":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party acceptchallenge <challenger>");
                    return true;
                }
                handleAcceptChallengeCommand(player, args[1]);
                break;
            case "declinechallenge":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party declinechallenge <challenger>");
                    return true;
                }
                handleDeclineChallengeCommand(player, args[1]);
                break;
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Party Commands ===");
        player.sendMessage("§e/party create §7- Create a new party");
        player.sendMessage("§e/party invite <player> §7- Invite a player to your party");
        player.sendMessage("§e/party join <leader> §7- Join a party");
        player.sendMessage("§e/party transfer <player> §7- Transfer party leadership");
        player.sendMessage("§e/party leave §7- Leave your current party");
        player.sendMessage("§e/party disband §7- Disband your party (leader only)");
        player.sendMessage("§e/party match §7- Start a match (leader only)");
        player.sendMessage("§e/party fight §7- Start a match (leader only)");
        player.sendMessage("§e/party challenge <leader> §7- Challenge another party");
        player.sendMessage("§e/party acceptduel <challenger> §7- Accept a party duel");
        player.sendMessage("§e/party declineduel <challenger> §7- Decline a party duel");
        player.sendMessage("§e/party acceptchallenge <challenger> §7- Accept a party duel challenge");
        player.sendMessage("§e/party declinechallenge <challenger> §7- Decline a party duel challenge");
        player.sendMessage("§e/party info §7- View party information");
    }
    
    private void handleCreateCommand(Player player) {
        if (plugin.getPartyManager().hasParty(player)) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }
        
        Party party = plugin.getPartyManager().createParty(player);
        if (party != null) {
            player.sendMessage("§aParty created! You are now the party leader.");
        } else {
            player.sendMessage("§cFailed to create party!");
        }
    }
    
    private void handleInviteCommand(Player player, String targetName) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can invite players!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or not online!");
            return;
        }
        
        if (plugin.getPartyManager().hasParty(target)) {
            player.sendMessage("§cThat player is already in a party!");
            return;
        }
        
        if (party.hasInvite(target.getUniqueId())) {
            player.sendMessage("§cThat player already has a pending invite!");
            return;
        }
        
        // Add invite with 60 second expiration
        long expirationTime = System.currentTimeMillis() + (60 * 1000);
        party.addInvite(target.getUniqueId(), expirationTime);
        
        // Send clickable invite message
        TextComponent message = new TextComponent("§a" + player.getName() + " §7has invited you to join their party! ");
        TextComponent acceptButton = new TextComponent("§a[ACCEPT]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + player.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join the party").create()));
        
        message.addExtra(acceptButton);
        target.spigot().sendMessage(message);
        
        player.sendMessage("§aInvite sent to " + target.getName() + "!");
    }
    
    private void handleJoinCommand(Player player, String leaderName) {
        if (plugin.getPartyManager().hasParty(player)) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }
        
        Player leader = Bukkit.getPlayer(leaderName);
        if (leader == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }
        
        Party party = plugin.getPartyManager().getPartyByLeader(leader.getUniqueId());
        if (party == null) {
            player.sendMessage("§cThat player doesn't have a party!");
            return;
        }
        
        if (!party.hasInvite(player.getUniqueId())) {
            player.sendMessage("§cYou don't have an invite to that party!");
            return;
        }
        
        plugin.getPartyManager().joinParty(player, party);
        
        // Notify all party members
        for (Player member : party.getOnlineMembers()) {
            member.sendMessage("§a" + player.getName() + " §7has joined the party!");
        }
    }
    
    private void handleTransferCommand(Player player, String targetName) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can transfer leadership!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or not online!");
            return;
        }
        
        if (!party.isMember(target.getUniqueId())) {
            player.sendMessage("§cThat player is not in your party!");
            return;
        }
        
        plugin.getPartyManager().transferLeadership(party, target.getUniqueId());
        
        // Notify all party members
        for (Player member : party.getOnlineMembers()) {
            member.sendMessage("§a" + target.getName() + " §7is now the party leader!");
        }
    }
    
    private void handleLeaveCommand(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        plugin.getPartyManager().leaveParty(player);
        player.sendMessage("§aYou have left the party!");
        
        // Notify remaining members
        for (Player member : party.getOnlineMembers()) {
            if (!member.equals(player)) {
                member.sendMessage("§c" + player.getName() + " §7has left the party!");
            }
        }
    }
    
    private void handleDisbandCommand(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can disband the party!");
            return;
        }
        
        // Notify all members
        for (Player member : party.getOnlineMembers()) {
            member.sendMessage("§cThe party has been disbanded!");
        }
        
        plugin.getPartyManager().disbandParty(party);
    }
    
    private void handleMatchCommand(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can start matches!");
            return;
        }
        
        if (party.isInMatch()) {
            player.sendMessage("§cYour party is already in a match!");
            return;
        }
        
        plugin.getGuiManager().openMatchTypeGui(player);
    }
    
    private void handleInfoCommand(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        player.sendMessage("§6=== Party Information ===");
        Player leader = Bukkit.getPlayer(party.getLeader());
        player.sendMessage("§eLeader: §a" + (leader != null ? leader.getName() : "Unknown"));
        player.sendMessage("§eMembers (" + party.getSize() + "):");
        
        for (Player member : party.getOnlineMembers()) {
            String status = party.isLeader(member.getUniqueId()) ? " §6(Leader)" : "";
            player.sendMessage("§7- §a" + member.getName() + status);
        }
        
        if (party.isInMatch()) {
            player.sendMessage("§eStatus: §cIn Match");
        } else {
            player.sendMessage("§eStatus: §aReady");
        }
    }

    private void handleChallengeCommand(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or not online!");
            return;
        }

        // Open kit selection GUI for party vs party
        plugin.getGuiManager().openPartyVsPartyKitGui(player, target);
    }

    private void handleAcceptDuelCommand(Player player, String challengerName) {
        plugin.getPartyDuelManager().acceptDuel(player, challengerName);
    }

    private void handleDeclineDuelCommand(Player player, String challengerName) {
        plugin.getPartyDuelManager().declineDuel(player, challengerName);
    }

    private void handleAcceptChallengeCommand(Player player, String challengerName) {
        plugin.getPartyDuelManager().acceptDuel(player, challengerName);
    }

    private void handleDeclineChallengeCommand(Player player, String challengerName) {
        plugin.getPartyDuelManager().declineDuel(player, challengerName);
    }
}
