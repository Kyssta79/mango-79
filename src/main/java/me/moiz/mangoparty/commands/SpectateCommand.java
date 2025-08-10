package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {
    private MangoParty plugin;
    
    public SpectateCommand(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        Match match = plugin.getMatchManager().getPlayerMatch(player);
        
        if (match == null) {
            player.sendMessage("§cYou are not in a match!");
            return true;
        }
        
        if (!match.isPlayerSpectator(player.getUniqueId())) {
            player.sendMessage("§cYou must be spectating to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cUsage: /spectate <player>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cPlayer not found or not online!");
            return true;
        }
        
        Match targetMatch = plugin.getMatchManager().getPlayerMatch(target);
        if (targetMatch == null || !targetMatch.getId().equals(match.getId())) {
            player.sendMessage("§cThat player is not in your match!");
            return true;
        }
        
        if (!match.isPlayerAlive(target.getUniqueId())) {
            player.sendMessage("§cThat player is not alive!");
            return true;
        }
        
        // Teleport to target
        player.teleport(target.getLocation());
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.sendMessage("§aNow spectating " + target.getName());
        
        return true;
    }
}
