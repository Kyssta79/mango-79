package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class MangoCommand implements CommandExecutor {
    private MangoParty plugin;
    
    public MangoCommand(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mangoparty.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("arena")) {
            if (args.length < 2) {
                sendArenaHelp(player);
                return true;
            }
            
            if (args[1].equalsIgnoreCase("editor")) {
                plugin.getArenaEditorGui().openArenaListGui(player);
                return true;
            }
            
            handleArenaCommand(player, args);
        } else if (args[0].equalsIgnoreCase("kit")) {
            if (args.length < 2) {
                sendKitHelp(player);
                return true;
            }
            
            if (args[1].equalsIgnoreCase("editor")) {
                plugin.getKitEditorGui().openKitListGui(player);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("create") && args.length > 1 && args[1].equalsIgnoreCase("kit")) {
            if (args.length < 3) {
                player.sendMessage("§cUsage: /mango create kit <name>");
                return true;
            }
            handleCreateKitCommand(player, args[2]);
        } else if (args[0].equalsIgnoreCase("addkitgui")) {
            if (args.length < 3) {
                player.sendMessage("§cUsage: /mango addkitgui <kit_name> <match_type> [slot]");
                return true;
            }
            handleAddKitGuiCommand(player, args);
        } else if (args[0].equalsIgnoreCase("setspawn")) {
            handleSetSpawnCommand(player);
        } else if (args[0].equalsIgnoreCase("reload")) {
            handleReloadCommand(player);
        } else {
            sendHelpMessage(player);
        }
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== MangoParty Admin Commands ===");
        player.sendMessage("§e/mango reload §7- Reload plugin configurations");
        player.sendMessage("§e/mango arena editor §7- Open arena editor GUI");
        player.sendMessage("§e/mango kit editor §7- Open kit editor GUI");
        player.sendMessage("§e/mango arena create <name> §7- Create a new arena");
        player.sendMessage("§e/mango arena corner1 <name> §7- Set arena corner 1");
        player.sendMessage("§e/mango arena corner2 <name> §7- Set arena corner 2");
        player.sendMessage("§e/mango arena center <name> §7- Set arena center");
        player.sendMessage("§e/mango arena spawn1 <name> §7- Set arena spawn 1");
        player.sendMessage("§e/mango arena spawn2 <name> §7- Set arena spawn 2");
        player.sendMessage("§e/mango arena save <name> §7- Save arena schematic");
        player.sendMessage("§e/mango arena list §7- List all arenas");
        player.sendMessage("§e/mango arena delete <name> §7- Delete an arena");
        player.sendMessage("§e/mango create kit <name> §7- Create kit from inventory");
        player.sendMessage("§e/mango addkitgui <kit_name> <match_type> [slot] §7- Add a kit to a GUI");
        player.sendMessage("§e/mango setspawn §7- Set the server spawn location");
    }
    
    private void sendArenaHelp(Player player) {
        player.sendMessage("§6=== Arena Commands ===");
        player.sendMessage("§e/mango arena editor §7- Open arena editor GUI");
        player.sendMessage("§e/mango arena create <name> §7- Create a new arena");
        player.sendMessage("§e/mango arena corner1 <name> §7- Set arena corner 1");
        player.sendMessage("§e/mango arena corner2 <name> §7- Set arena corner 2");
        player.sendMessage("§e/mango arena center <name> §7- Set arena center");
        player.sendMessage("§e/mango arena spawn1 <name> §7- Set arena spawn 1");
        player.sendMessage("§e/mango arena spawn2 <name> §7- Set arena spawn 2");
        player.sendMessage("§e/mango arena save <name> §7- Save arena schematic");
        player.sendMessage("§e/mango arena list §7- List all arenas");
        player.sendMessage("§e/mango arena delete <name> §7- Delete an arena");
    }
    
    private void sendKitHelp(Player player) {
        player.sendMessage("§6=== Kit Commands ===");
        player.sendMessage("§e/mango kit editor §7- Open kit editor GUI");
    }
    
    private void handleArenaCommand(Player player, String[] args) {
        String subCommand = args[1].toLowerCase();
        
        if (args.length < 3) {
            if (subCommand.equals("list")) {
                // Allow /mango arena list without arena name
            } else {
                player.sendMessage("§cPlease specify an arena name!");
                return;
            }
        }
        
        String arenaName = (args.length > 2) ? args[2] : null;
        
        switch (subCommand) {
            case "create":
                handleArenaCreate(player, arenaName);
                break;
            case "corner1":
                handleArenaCorner1(player, arenaName);
                break;
            case "corner2":
                handleArenaCorner2(player, arenaName);
                break;
            case "center":
                handleArenaCenter(player, arenaName);
                break;
            case "spawn1":
                handleArenaSpawn1(player, arenaName);
                break;
            case "spawn2":
                handleArenaSpawn2(player, arenaName);
                break;
            case "save":
                handleArenaSave(player, arenaName);
                break;
            case "list":
                handleArenaList(player);
                break;
            case "delete":
                handleArenaDelete(player, arenaName);
                break;
            default:
                sendArenaHelp(player);
                break;
        }
    }
    
    private void handleArenaCreate(Player player, String arenaName) {
        Arena existingArena = plugin.getArenaManager().getArena(arenaName);
        if (existingArena != null) {
            player.sendMessage("§cArena with that name already exists!");
            return;
        }
        
        Arena arena = plugin.getArenaManager().createArena(arenaName, player.getWorld().getName());
        player.sendMessage("§aArena '" + arenaName + "' created!");
    }
    
    private void handleArenaCorner1(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        arena.setCorner1(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage("§aCorner 1 set for arena '" + arenaName + "'!");
    }
    
    private void handleArenaCorner2(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        arena.setCorner2(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage("§aCorner 2 set for arena '" + arenaName + "'!");
    }
    
    private void handleArenaCenter(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        arena.setCenter(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage("§aCenter set for arena '" + arenaName + "'!");
    }
    
    private void handleArenaSpawn1(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        arena.setSpawn1(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage("§aSpawn 1 set for arena '" + arenaName + "'!");
    }
    
    private void handleArenaSpawn2(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        arena.setSpawn2(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        player.sendMessage("§aSpawn 2 set for arena '" + arenaName + "'!");
    }
    
    private void handleArenaSave(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found! Create it first with /mango arena create " + arenaName);
            return;
        }
        
        if (!arena.isComplete()) {
            player.sendMessage("§cArena is not complete! Set all corners, center, and spawns first.");
            return;
        }
        
        plugin.getArenaManager().saveArenaSchematic(arena);
        player.sendMessage("§aSchematic saved for arena '" + arenaName + "'!");
    }
    
    private void handleCreateKitCommand(Player player, String kitName) {
        if (plugin.getKitManager().getKit(kitName) != null) {
            player.sendMessage("§cKit with that name already exists!");
            return;
        }
        
        plugin.getKitManager().createKit(kitName, player);
        player.sendMessage("§aKit '" + kitName + "' created from your current inventory!");
    }

    private void handleArenaList(Player player) {
        Map<String, Arena> arenas = plugin.getArenaManager().getArenas();
        if (arenas.isEmpty()) {
            player.sendMessage("§cNo arenas found!");
            return;
        }
        
        player.sendMessage("§6=== Arena List ===");
        for (Arena arena : arenas.values()) {
            String status = arena.isComplete() ? "§aComplete" : "§cIncomplete";
            player.sendMessage("§e" + arena.getName() + " §7- " + status);
        }
    }

    private void handleArenaDelete(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found!");
            return;
        }
        
        // Remove from manager and config
        plugin.getArenaManager().deleteArena(arenaName);
        player.sendMessage("§aArena '" + arenaName + "' deleted!");
    }

    private void handleAddKitGuiCommand(Player player, String[] args) {
        String kitName = args[1];
        String matchType = args[2].toLowerCase();
        Integer slot = null;

        if (args.length > 3) {
            try {
                slot = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid slot number. Must be an integer.");
                return;
            }
        }

        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit '" + kitName + "' not found!");
            return;
        }

        // Support queue modes
        if (matchType.equals("1v1") || matchType.equals("2v2") || matchType.equals("3v3")) {
            boolean success = plugin.getConfigManager().addKitToQueueGuiConfig(kit, matchType, slot);
            if (success) {
                player.sendMessage("§aKit '" + kitName + "' added to " + matchType.toUpperCase() + " queue GUI!");
                plugin.getGuiManager().reloadGuiConfigs();
            } else {
                player.sendMessage("§cFailed to add kit '" + kitName + "' to " + matchType.toUpperCase() + " queue GUI.");
            }
            return;
        }

        if (!matchType.equals("split") && !matchType.equals("ffa")) {
            player.sendMessage("§cInvalid match type. Must be 'split', 'ffa', '1v1', '2v2', or '3v3'.");
            return;
        }

        boolean success = plugin.getConfigManager().addKitToGuiConfig(kit, matchType, slot);
        if (success) {
            player.sendMessage("§aKit '" + kitName + "' added to " + matchType.toUpperCase() + " GUI!");
            plugin.getGuiManager().reloadGuiConfigs(); // Reload GUI to reflect changes
        } else {
            player.sendMessage("§cFailed to add kit '" + kitName + "' to " + matchType.toUpperCase() + " GUI. It might already be there or an invalid slot was provided.");
        }
    }

    private void handleSetSpawnCommand(Player player) {
        plugin.setSpawnLocation(player.getLocation());
        player.sendMessage("§aSpawn location set to your current position!");
    }

    private void handleReloadCommand(Player player) {
        // Reload kit editor configs
        plugin.getKitEditorGui().reloadConfigs();
        plugin.getGuiManager().reloadGuiConfigs();
        
        player.sendMessage("§aMangoParty configurations reloaded!");
    }
}
