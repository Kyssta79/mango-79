package me.moiz.mangoparty.config;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class ConfigManager {
    private MangoParty plugin;
    
    public ConfigManager(MangoParty plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Create default configuration files
        createDefaultConfig();
        createDefaultGuiConfigs();
        createDefaultArenaConfig();
    }
    
    private void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
    }
    
    private void createDefaultGuiConfigs() {
        File guiDir = new File(plugin.getDataFolder(), "gui");
        if (!guiDir.exists()) {
            guiDir.mkdirs();
        }
        
        // Create split.yml
        File splitFile = new File(guiDir, "split.yml");
        if (!splitFile.exists()) {
            plugin.saveResource("gui/split.yml", false);
        }
        
        // Create ffa.yml
        File ffaFile = new File(guiDir, "ffa.yml");
        if (!ffaFile.exists()) {
            plugin.saveResource("gui/ffa.yml", false);
        }
        
        // Create queue GUI files
        String[] queueTypes = {"1v1", "2v2", "3v3"};
        for (String queueType : queueTypes) {
            File queueFile = new File(guiDir, queueType + "kits.yml");
            if (!queueFile.exists()) {
                createDefaultQueueConfig(queueFile, queueType);
            }
        }
    }
    
    private void createDefaultArenaConfig() {
        File arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            YamlConfiguration arenasConfig = new YamlConfiguration();
            arenasConfig.set("arenas", ""); // Empty arenas section
            
            try {
                arenasConfig.save(arenasFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create arenas.yml: " + e.getMessage());
            }
        }
    }

    public boolean addKitToGuiConfig(Kit kit, String matchType, Integer slot) {
        File guiDir = new File(plugin.getDataFolder(), "gui");
        if (!guiDir.exists()) {
            guiDir.mkdirs();
        }

        File configFile = new File(guiDir, matchType + ".yml");
        if (!configFile.exists()) {
            plugin.saveResource("gui/" + matchType + ".yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Find next available slot if not specified
        if (slot == null) {
            slot = findNextAvailableSlot(config);
        }

        // Check if slot is already taken
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String key : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
                if (kitSection != null && kitSection.getInt("slot") == slot) {
                    return false; // Slot already taken
                }
            }
        }

        // Add the kit
        String kitKey = kit.getName().toLowerCase().replace(" ", "_");
        config.set("kits." + kitKey + ".kit", kit.getName());
        config.set("kits." + kitKey + ".slot", slot);
        config.set("kits." + kitKey + ".name", "&e" + kit.getDisplayName());
        
        // Set empty lore (no default lore)
        config.set("kits." + kitKey + ".lore", new ArrayList<String>());

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + matchType + ".yml: " + e.getMessage());
            return false;
        }
    }

    public boolean addKitToQueueGuiConfig(Kit kit, String mode, Integer slot) {
        File guiDir = new File(plugin.getDataFolder(), "gui");
        if (!guiDir.exists()) {
            guiDir.mkdirs();
        }

        File configFile = new File(guiDir, mode + "kits.yml");
        if (!configFile.exists()) {
            createDefaultQueueConfig(configFile, mode);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Find next available slot if not specified
        if (slot == null) {
            slot = findNextAvailableSlot(config);
        }

        // Check if slot is already taken
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String key : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
                if (kitSection != null && kitSection.getInt("slot") == slot) {
                    return false; // Slot already taken
                }
            }
        }

        // Add the kit
        config.set("kits." + kit.getName() + ".slot", slot);
        config.set("kits." + kit.getName() + ".material", kit.getIcon() != null ? kit.getIcon().getType().name() : "IRON_SWORD");
        config.set("kits." + kit.getName() + ".name", "&e" + kit.getDisplayName());
        
        // Set empty lore (no default lore)
        config.set("kits." + kit.getName() + ".lore", new ArrayList<String>());

        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + mode + "kits.yml: " + e.getMessage());
            return false;
        }
    }

    private void createDefaultQueueConfig(File configFile, String mode) {
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("title", "&6" + mode.toUpperCase() + " Kit Selection");
        config.set("size", 27);
        config.set("kits", "");
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create " + configFile.getName() + ": " + e.getMessage());
        }
    }

    private int findNextAvailableSlot(YamlConfiguration config) {
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection == null) {
            return 0;
        }

        boolean[] usedSlots = new boolean[54]; // Max inventory size
        
        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
            if (kitSection != null) {
                int slot = kitSection.getInt("slot", -1);
                if (slot >= 0 && slot < 54) {
                    usedSlots[slot] = true;
                }
            }
        }

        // Find first available slot
        for (int i = 0; i < 54; i++) {
            if (!usedSlots[i]) {
                return i;
            }
        }

        return 0; // Fallback
    }

    public boolean removeKitFromGuiConfig(String kitName, String guiType) {
        try {
            File guiFile = new File(plugin.getDataFolder(), "gui/" + guiType + ".yml");
            if (!guiFile.exists()) {
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null || !kitsSection.contains(kitName.toLowerCase().replace(" ", "_"))) {
                return false;
            }

            kitsSection.set(kitName.toLowerCase().replace(" ", "_"), null);
            config.save(guiFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove kit from GUI config: " + e.getMessage());
            return false;
        }
    }

    public boolean removeKitFromQueueGuiConfig(String kitName, String queueType) {
        try {
            File guiFile = new File(plugin.getDataFolder(), "gui/" + queueType + "kits.yml");
            if (!guiFile.exists()) {
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null || !kitsSection.contains(kitName)) {
                return false;
            }

            kitsSection.set(kitName, null);
            config.save(guiFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove kit from queue GUI config: " + e.getMessage());
            return false;
        }
    }

    public Integer getKitSlotInGui(String kitName, String guiType) {
        try {
            String fileName = guiType;
            if (guiType.equals("1v1") || guiType.equals("2v2") || guiType.equals("3v3")) {
                fileName = guiType + "kits";
            }
            
            File guiFile = new File(plugin.getDataFolder(), "gui/" + fileName + ".yml");
            if (!guiFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null || !kitsSection.contains(kitName.toLowerCase().replace(" ", "_"))) {
                return null;
            }

            return kitsSection.getInt(kitName.toLowerCase().replace(" ", "_") + ".slot", -1);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get kit slot: " + e.getMessage());
            return null;
        }
    }

    public String getKitAtSlot(String guiType, int slot) {
        try {
            String fileName = guiType;
            if (guiType.equals("1v1") || guiType.equals("2v2") || guiType.equals("3v3")) {
                fileName = guiType + "kits";
            }
            
            File guiFile = new File(plugin.getDataFolder(), "gui/" + fileName + ".yml");
            if (!guiFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null) {
                return null;
            }

            for (String key : kitsSection.getKeys(false)) {
                if (kitsSection.getInt(key + ".slot") == slot) {
                    return kitsSection.getString(key + ".kit");
                }
            }

            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get kit at slot: " + e.getMessage());
            return null;
        }
    }

    public boolean updateKitSlotInGui(String kitName, String guiType, int newSlot) {
        try {
            File guiFile = new File(plugin.getDataFolder(), "gui/" + guiType + ".yml");
            if (!guiFile.exists()) {
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null || !kitsSection.contains(kitName.toLowerCase().replace(" ", "_"))) {
                return false;
            }

            kitsSection.set(kitName.toLowerCase().replace(" ", "_") + ".slot", newSlot);
            config.save(guiFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update kit slot: " + e.getMessage());
            return false;
        }
    }

    public boolean updateKitSlotInQueueGui(String kitName, String queueType, int newSlot) {
        try {
            File guiFile = new File(plugin.getDataFolder(), "gui/" + queueType + "kits.yml");
            if (!guiFile.exists()) {
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
            ConfigurationSection kitsSection = config.getConfigurationSection("kits");
            
            if (kitsSection == null || !kitsSection.contains(kitName)) {
                return false;
            }

            kitsSection.set(kitName + ".slot", newSlot);
            config.save(guiFile);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update kit slot in queue GUI: " + e.getMessage());
            return false;
        }
    }

    public void updateKitIconInAllGuis(Kit kit) {
        String[] guiTypes = {"split", "ffa", "1v1kits", "2v2kits", "3v3kits"};
        
        for (String guiType : guiTypes) {
            try {
                File guiFile = new File(plugin.getDataFolder(), "gui/" + guiType + ".yml");
                if (!guiFile.exists()) continue;

                YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
                ConfigurationSection kitsSection = config.getConfigurationSection("kits");
                
                if (kitsSection != null && kitsSection.contains(kit.getName().toLowerCase().replace(" ", "_"))) {
                    kitsSection.set(kit.getName().toLowerCase().replace(" ", "_") + ".material", kit.getIcon() != null ? kit.getIcon().getType().name() : "IRON_SWORD");
                    config.save(guiFile);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to update kit icon in " + guiType + ": " + e.getMessage());
            }
        }
    }
}
