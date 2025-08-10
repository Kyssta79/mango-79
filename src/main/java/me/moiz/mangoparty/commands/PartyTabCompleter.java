package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PartyTabCompleter implements TabCompleter {
    private MangoParty plugin;
    
    public PartyTabCompleter(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: party commands
            List<String> commands = Arrays.asList("create", "invite", "join", "transfer", "leave", "disband", "match", "fight", "info", "challenge");
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                // Suggest online players who are not in a party
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .filter(p -> !plugin.getPartyManager().hasParty(p))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("join")) {
                // Suggest online players who have parties and have invited this player
                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .filter(p -> {
                            Party party = plugin.getPartyManager().getPartyByLeader(p.getUniqueId());
                            return party != null && party.hasInvite(player.getUniqueId());
                        })
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("transfer")) {
                // Suggest party members (excluding the leader)
                Party party = plugin.getPartyManager().getParty(player);
                if (party != null && party.isLeader(player.getUniqueId())) {
                    return party.getOnlineMembers().stream()
                            .filter(p -> !p.equals(player))
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        
        return completions;
    }
}
