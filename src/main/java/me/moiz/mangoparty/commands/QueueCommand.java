package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {
    private MangoParty plugin;
    private String mode;
    
    public QueueCommand(MangoParty plugin, String mode) {
        this.plugin = plugin;
        this.mode = mode;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Open kit selection GUI for the specific mode
        plugin.getGuiManager().openQueueKitGui(player, mode);
        
        return true;
    }
}
