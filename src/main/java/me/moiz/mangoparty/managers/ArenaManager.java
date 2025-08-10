package me.moiz.mangoparty.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ArenaManager {
    private MangoParty plugin;
    private Map<String, Arena> arenas;
    private Set<String> reservedArenas;
    private File arenasFile;
    private YamlConfiguration arenasConfig;
    private File schematicsDir;
    private boolean debugMode;

    public ArenaManager(MangoParty plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.reservedArenas = new HashSet<>();
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        this.schematicsDir = new File(plugin.getDataFolder(), "schematics");
        this.debugMode = plugin.getConfig().getBoolean("debug", false);
        
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
            debugLog("Created schematics directory: " + schematicsDir.getPath());
        }
        
        loadArenas();
    }

    private void debugLog(String message) {
        if (debugMode) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    private void loadArenas() {
        if (!arenasFile.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
        
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String arenaName : arenasSection.getKeys(false)) {
                Arena arena = loadArenaFromConfig(arenaName, arenasSection.getConfigurationSection(arenaName));
                if (arena != null) {
                    arenas.put(arenaName, arena);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas");
    }

    private Arena loadArenaFromConfig(String name, ConfigurationSection section) {
        try {
            String worldName = section.getString("world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for arena '" + name + "'");
                return null;
            }

            Arena arena = new Arena(name, worldName);
            
            // Load locations
            if (section.contains("center")) {
                arena.setCenter(loadLocation(section.getConfigurationSection("center"), world));
            }
            if (section.contains("spawn1")) {
                arena.setSpawn1(loadLocation(section.getConfigurationSection("spawn1"), world));
            }
            if (section.contains("spawn2")) {
                arena.setSpawn2(loadLocation(section.getConfigurationSection("spawn2"), world));
            }
            if (section.contains("corner1")) {
                arena.setCorner1(loadLocation(section.getConfigurationSection("corner1"), world));
            }
            if (section.contains("corner2")) {
                arena.setCorner2(loadLocation(section.getConfigurationSection("corner2"), world));
            }
            
            // Load allowed kits
            if (section.contains("allowedKits")) {
                arena.setAllowedKits(section.getStringList("allowedKits"));
            }
            
            // Load regenerate blocks setting
            arena.setRegenerateBlocks(section.getBoolean("regenerateBlocks", true));
            
            return arena;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load arena '" + name + "': " + e.getMessage());
            return null;
        }
    }

    private Location loadLocation(ConfigurationSection section, World world) {
        if (section == null) return null;
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0.0);
        float pitch = (float) section.getDouble("pitch", 0.0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void saveArenas() {
        arenasConfig.set("arenas", null); // Clear existing data
        
        for (Arena arena : arenas.values()) {
            saveArenaToConfig(arena);
        }
        
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arenas.yml: " + e.getMessage());
        }
    }

    private void saveArenaToConfig(Arena arena) {
        String path = "arenas." + arena.getName();
        
        arenasConfig.set(path + ".world", arena.getWorldName());
        
        if (arena.getCenter() != null) {
            saveLocationToConfig(path + ".center", arena.getCenter());
        }
        if (arena.getSpawn1() != null) {
            saveLocationToConfig(path + ".spawn1", arena.getSpawn1());
        }
        if (arena.getSpawn2() != null) {
            saveLocationToConfig(path + ".spawn2", arena.getSpawn2());
        }
        if (arena.getCorner1() != null) {
            saveLocationToConfig(path + ".corner1", arena.getCorner1());
        }
        if (arena.getCorner2() != null) {
            saveLocationToConfig(path + ".corner2", arena.getCorner2());
        }
        
        arenasConfig.set(path + ".allowedKits", arena.getAllowedKits());
        arenasConfig.set(path + ".regenerateBlocks", arena.isRegenerateBlocks());
    }

    private void saveLocationToConfig(String path, Location location) {
        arenasConfig.set(path + ".x", location.getX());
        arenasConfig.set(path + ".y", location.getY());
        arenasConfig.set(path + ".z", location.getZ());
        arenasConfig.set(path + ".yaw", location.getYaw());
        arenasConfig.set(path + ".pitch", location.getPitch());
    }

    public Arena createArena(String name) {
        return createArena(name, "world");
    }

    public Arena createArena(String name, String worldName) {
        if (arenas.containsKey(name)) {
            return null;
        }
        
        Arena arena = new Arena(name, worldName);
        // By default, all kits should be disabled (empty list means all disabled)
        arena.setAllowedKits(new ArrayList<>());
        arenas.put(name, arena);
        saveArena(arena);
        
        plugin.getLogger().info("Created new arena: " + name);
        return arena;
    }

    public void saveArena(Arena arena) {
        saveArenaToConfig(arena);
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arena '" + arena.getName() + "': " + e.getMessage());
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Map<String, Arena> getArenas() {
        return new HashMap<>(arenas);
    }

    public List<Arena> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }

    public void deleteArena(String name) {
        Arena arena = arenas.remove(name);
        if (arena != null) {
            arenasConfig.set("arenas." + name, null);
            try {
                arenasConfig.save(arenasFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to delete arena '" + name + "': " + e.getMessage());
            }
            
            // Delete both .schem and .schematic files if they exist
            File schemFile = new File(schematicsDir, name + ".schem");
            File schematicFile = new File(schematicsDir, name + ".schematic");
            
            if (schemFile.exists()) {
                schemFile.delete();
                debugLog("Deleted .schem file for arena: " + name);
            }
            if (schematicFile.exists()) {
                schematicFile.delete();
                debugLog("Deleted .schematic file for arena: " + name);
            }
            
            plugin.getLogger().info("Deleted arena: " + name);
        }
    }

    public Arena getAvailableArena() {
        for (Arena arena : arenas.values()) {
            if (arena.isComplete() && !reservedArenas.contains(arena.getName())) {
                return arena;
            }
        }
        return null;
    }

    public boolean reserveArena(String arenaName) {
        Arena arena = arenas.get(arenaName);
        if (arena != null && arena.isComplete() && !reservedArenas.contains(arenaName)) {
            reservedArenas.add(arenaName);
            return true;
        }
        return false;
    }

    public void releaseArena(String arenaName) {
        reservedArenas.remove(arenaName);
    }

    public boolean isArenaReserved(String arenaName) {
        return reservedArenas.contains(arenaName);
    }

    public boolean saveSchematic(Arena arena) {
        if (!arena.isComplete()) {
            debugLog("Cannot save schematic for incomplete arena: " + arena.getName());
            return false;
        }

        try {
            Location corner1 = arena.getCorner1();
            Location corner2 = arena.getCorner2();
            
            if (corner1 == null || corner2 == null) {
                debugLog("Corner locations are null for arena: " + arena.getName());
                return false;
            }

            debugLog("Saving schematic for arena: " + arena.getName());
            debugLog("Corner1: " + corner1.getBlockX() + ", " + corner1.getBlockY() + ", " + corner1.getBlockZ());
            debugLog("Corner2: " + corner2.getBlockX() + ", " + corner2.getBlockY() + ", " + corner2.getBlockZ());

            // Create WorldEdit region
            BlockVector3 min = BlockVector3.at(
                Math.min(corner1.getBlockX(), corner2.getBlockX()),
                Math.min(corner1.getBlockY(), corner2.getBlockY()),
                Math.min(corner1.getBlockZ(), corner2.getBlockZ())
            );
            
            BlockVector3 max = BlockVector3.at(
                Math.max(corner1.getBlockX(), corner2.getBlockX()),
                Math.max(corner1.getBlockY(), corner2.getBlockY()),
                Math.max(corner1.getBlockZ(), corner2.getBlockZ())
            );

            debugLog("Region min: " + min + ", max: " + max);

            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(corner1.getWorld()), min, max);
            
            // Create clipboard using BlockArrayClipboard
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(corner1.getWorld()))) {
                debugLog("Creating ForwardExtentCopy operation...");
                ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
                Operations.complete(copy);
                debugLog("Copy operation completed successfully");
                
                // Try both .schem and .schematic formats
                File schemFile = new File(schematicsDir, arena.getName() + ".schem");
                File schematicFile = new File(schematicsDir, arena.getName() + ".schematic");
                
                debugLog("Attempting to save to: " + schemFile.getPath());
                
                // First try .schem format (newer format)
                ClipboardFormat schemFormat = ClipboardFormats.findByFile(schemFile);
                if (schemFormat != null) {
                    debugLog("Using format: " + schemFormat.getName());
                    try (FileOutputStream fos = new FileOutputStream(schemFile);
                         ClipboardWriter writer = schemFormat.getWriter(fos)) {
                        writer.write(clipboard);
                        fos.flush();
                        debugLog("Successfully wrote .schem file, size: " + schemFile.length() + " bytes");
                    }
                } else {
                    debugLog("Could not find .schem format, trying .schematic format");
                    // Try .schematic format (legacy format)
                    ClipboardFormat schematicFormat = ClipboardFormats.findByFile(schematicFile);
                    if (schematicFormat != null) {
                        debugLog("Using legacy format: " + schematicFormat.getName());
                        try (FileOutputStream fos = new FileOutputStream(schematicFile);
                             ClipboardWriter writer = schematicFormat.getWriter(fos)) {
                            writer.write(clipboard);
                            fos.flush();
                            debugLog("Successfully wrote .schematic file, size: " + schematicFile.length() + " bytes");
                        }
                    } else {
                        plugin.getLogger().severe("Could not find any suitable clipboard format!");
                        return false;
                    }
                }
            }
            
            plugin.getLogger().info("Saved schematic for arena: " + arena.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save schematic for arena '" + arena.getName() + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean pasteSchematic(String arenaName, Location location) {
        // Check for both .schem and .schematic files
        File schemFile = new File(schematicsDir, arenaName + ".schem");
        File schematicFile = new File(schematicsDir, arenaName + ".schematic");
        
        File fileToUse = null;
        if (schemFile.exists() && schemFile.length() > 0) {
            fileToUse = schemFile;
            debugLog("Using .schem file: " + schemFile.getPath() + " (size: " + schemFile.length() + " bytes)");
        } else if (schematicFile.exists() && schematicFile.length() > 0) {
            fileToUse = schematicFile;
            debugLog("Using .schematic file: " + schematicFile.getPath() + " (size: " + schematicFile.length() + " bytes)");
        } else {
            plugin.getLogger().warning("No valid schematic file found for arena: " + arenaName);
            debugLog("Checked files:");
            debugLog("  " + schemFile.getPath() + " - exists: " + schemFile.exists() + ", size: " + (schemFile.exists() ? schemFile.length() : "N/A"));
            debugLog("  " + schematicFile.getPath() + " - exists: " + schematicFile.exists() + ", size: " + (schematicFile.exists() ? schematicFile.length() : "N/A"));
            return false;
        }

        try {
            debugLog("Attempting to paste schematic: " + fileToUse.getName());
            ClipboardFormat format = ClipboardFormats.findByFile(fileToUse);
            if (format == null) {
                plugin.getLogger().warning("Could not find clipboard format for: " + fileToUse.getName());
                return false;
            }

            debugLog("Using format: " + format.getName());

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(fileToUse))) {
                clipboard = reader.read();
                debugLog("Successfully read clipboard from file");
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                debugLog("Creating paste operation at: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .build();
                
                Operations.complete(operation);
                debugLog("Paste operation completed successfully");
                plugin.getLogger().info("Successfully pasted schematic '" + arenaName + "' at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            }
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to paste schematic '" + arenaName + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean pasteSchematic(Arena arena, Location location) {
        return pasteSchematic(arena.getName(), location);
    }

    // CRITICAL: This is what MatchManager was looking for
    public boolean pasteSchematic(Arena arena) {
        if (arena == null || arena.getCenter() == null) {
            return false;
        }
        return pasteSchematic(arena.getName(), arena.getCenter());
    }

    public void saveArenaSchematic(Arena arena) {
        saveSchematic(arena);
    }

    public String cloneArena(Arena originalArena, Location newLocation) {
        if (!originalArena.isComplete()) {
            plugin.getLogger().warning("Cannot clone incomplete arena: " + originalArena.getName());
            return null;
        }

        debugLog("Starting clone operation for arena: " + originalArena.getName());

        // Generate unique name for clone
        String baseName = originalArena.getName() + "_clone";
        String cloneName = baseName;
        int counter = 1;
        
        while (arenas.containsKey(cloneName)) {
            cloneName = baseName + "_" + counter;
            counter++;
        }

        debugLog("Generated clone name: " + cloneName);

        // Create new arena
        Arena clonedArena = createArena(cloneName, newLocation.getWorld().getName());
        if (clonedArena == null) {
            plugin.getLogger().severe("Failed to create cloned arena: " + cloneName);
            return null;
        }

        // Calculate offset
        Location originalCenter = originalArena.getCenter();
        double offsetX = newLocation.getX() - originalCenter.getX();
        double offsetY = newLocation.getY() - originalCenter.getY();
        double offsetZ = newLocation.getZ() - originalCenter.getZ();

        debugLog("Calculated offset: X=" + offsetX + ", Y=" + offsetY + ", Z=" + offsetZ);

        // Set locations with offset
        clonedArena.setCenter(offsetLocation(originalArena.getCenter(), offsetX, offsetY, offsetZ, newLocation.getWorld().getName()));
        clonedArena.setSpawn1(offsetLocation(originalArena.getSpawn1(), offsetX, offsetY, offsetZ, newLocation.getWorld().getName()));
        clonedArena.setSpawn2(offsetLocation(originalArena.getSpawn2(), offsetX, offsetY, offsetZ, newLocation.getWorld().getName()));
        clonedArena.setCorner1(offsetLocation(originalArena.getCorner1(), offsetX, offsetY, offsetZ, newLocation.getWorld().getName()));
        clonedArena.setCorner2(offsetLocation(originalArena.getCorner2(), offsetX, offsetY, offsetZ, newLocation.getWorld().getName()));

        // Copy other properties
        clonedArena.setAllowedKits(new ArrayList<>(originalArena.getAllowedKits()));
        clonedArena.setRegenerateBlocks(originalArena.isRegenerateBlocks());

        // Save the cloned arena
        saveArena(clonedArena);

        // Check if original arena has a schematic
        File schemFile = new File(schematicsDir, originalArena.getName() + ".schem");
        File schematicFile = new File(schematicsDir, originalArena.getName() + ".schematic");
        
        boolean hasValidSchematic = (schemFile.exists() && schemFile.length() > 0) || 
                                   (schematicFile.exists() && schematicFile.length() > 0);

        if (!hasValidSchematic) {
            debugLog("No valid schematic found for original arena, creating one...");
            if (!saveSchematic(originalArena)) {
                plugin.getLogger().warning("Failed to save original schematic for arena: " + originalArena.getName());
                return cloneName; // Return the cloned arena name even if schematic fails
            }
        }

        // Paste the schematic at the new location
        if (!pasteSchematic(originalArena.getName(), newLocation)) {
            plugin.getLogger().warning("Failed to paste schematic for cloned arena: " + cloneName);
            // Don't return null here, the arena was still created successfully
        } else {
            plugin.getLogger().info("Successfully cloned arena '" + originalArena.getName() + "' as '" + cloneName + "'");
        }

        return cloneName;
    }

    private Location offsetLocation(Location original, double offsetX, double offsetY, double offsetZ, String worldName) {
        if (original == null) return null;
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        
        return new Location(world, 
            original.getX() + offsetX, 
            original.getY() + offsetY, 
            original.getZ() + offsetZ, 
            original.getYaw(), 
            original.getPitch());
    }

    public void cleanup() {
        saveArenas();
        reservedArenas.clear();
    }
}
