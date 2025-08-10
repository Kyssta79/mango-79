package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpectateTabCompleter implements TabCompleter {
    private MangoParty plugin;
    
    public SpectateTabCompleter(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null || !match.isPlayerSpectator(player.getUniqueId())) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Suggest alive players in the same match
            return match.getAllPlayers().stream()
                    .filter(p -> match.isPlayerAlive(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
