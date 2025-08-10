package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MangoTabCompleter implements TabCompleter {
    private MangoParty plugin;
    
    public MangoTabCompleter(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("mangoparty.admin")) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: main commands
            List<String> commands = Arrays.asList("arena", "kit", "create", "addkitgui", "setspawn", "reload");
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("arena")) {
                // Arena subcommands
                List<String> arenaCommands = Arrays.asList("editor", "create", "corner1", "corner2", "center", "spawn1", "spawn2", "save", "list", "delete");
                return arenaCommands.stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("kit")) {
                // Kit subcommands
                List<String> kitCommands = Arrays.asList("editor");
                return kitCommands.stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("create")) {
                // Create subcommands
                List<String> createCommands = Arrays.asList("kit");
                return createCommands.stream()
                        .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("addkitgui")) {
                // Suggest existing kit names
                return plugin.getKitManager().getKits().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("arena")) {
                if (args[1].equalsIgnoreCase("create")) {
                    // Suggest arena name (no specific suggestions, let them type)
                    return Arrays.asList("<arena_name>");
                } else if (Arrays.asList("corner1", "corner2", "center", "spawn1", "spawn2", "save", "delete").contains(args[1].toLowerCase())) {
                    // Suggest existing arena names
                    return plugin.getArenaManager().getArenas().keySet().stream()
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("kit")) {
                // Suggest kit name (no specific suggestions, let them type)
                return Arrays.asList("<kit_name>");
            } else if (args[0].equalsIgnoreCase("addkitgui")) {
                // Suggest match types
                List<String> matchTypes = Arrays.asList("split", "ffa");
                return matchTypes.stream()
                        .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("addkitgui")) {
                // Suggest a placeholder for slot
                return Arrays.asList("<slot>");
            }
        }
        
        return completions;
    }
}
