package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KitManager {
    private MangoParty plugin;
    private Map<String, Kit> kits;
    private File kitsDir;
    
    public KitManager(MangoParty plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.kitsDir = new File(plugin.getDataFolder(), "kits");
        
        if (!kitsDir.exists()) {
            kitsDir.mkdirs();
        }
        
        loadKits();
    }
    
    private void loadKits() {
        File[] kitFiles = kitsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles != null) {
            for (File kitFile : kitFiles) {
                String kitName = kitFile.getName().replace(".yml", "");
                Kit kit = loadKitFromFile(kitName, kitFile);
                if (kit != null) {
                    kits.put(kitName, kit);
                }
            }
        }
    }
    
    private Kit loadKitFromFile(String name, File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Kit kit = new Kit(name);
            
            kit.setDisplayName(config.getString("displayName", name));
            
            if (config.contains("contents")) {
                ItemStack[] contents = new ItemStack[36];
                for (int i = 0; i < 36; i++) {
                    if (config.contains("contents." + i)) {
                        contents[i] = config.getItemStack("contents." + i);
                    }
                }
                kit.setContents(contents);
            }
            
            if (config.contains("armor")) {
                ItemStack[] armor = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    if (config.contains("armor." + i)) {
                        armor[i] = config.getItemStack("armor." + i);
                    }
                }
                kit.setArmor(armor);
            }
            
            if (config.contains("icon")) {
                kit.setIcon(config.getItemStack("icon"));
            }
            
            // Load kit rules
            if (config.contains("rules")) {
                ConfigurationSection rulesSection = config.getConfigurationSection("rules");
                KitRules rules = new KitRules();
                
                rules.setNaturalHealthRegen(rulesSection.getBoolean("natural_health_regen", true));
                rules.setBlockBreak(rulesSection.getBoolean("block_break", false));
                rules.setBlockPlace(rulesSection.getBoolean("block_place", false));
                rules.setDamageMultiplier(rulesSection.getDouble("damage_multiplier", 1.0));
                rules.setInstantTnt(rulesSection.getBoolean("instant_tnt", false));
                
                kit.setRules(rules);
            }
            
            return kit;
        } catch (Exception e) {
            plugin.getLogger().warning("§c⚠️ Failed to load kit: " + name + " - " + e.getMessage());
            return null;
        }
    }
    
    public void createKit(String name, Player player) {
        Kit kit = new Kit(name);
        kit.setDisplayName(name);
        kit.setContents(player.getInventory().getContents());
        kit.setArmor(player.getInventory().getArmorContents());
        
        // Use first item in inventory as icon, or default to sword
        ItemStack icon = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                icon = item.clone();
                icon.setAmount(1);
                break;
            }
        }
        if (icon == null) {
            icon = new ItemStack(org.bukkit.Material.IRON_SWORD);
        }
        kit.setIcon(icon);
        
        kits.put(name, kit);
        saveKit(kit);
        plugin.getLogger().info("§a⚔️ Created new kit: §e" + name + " §7by §e" + player.getName());
    }
    
    public void saveKit(Kit kit) {
        File kitFile = new File(kitsDir, kit.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("displayName", kit.getDisplayName());
        
        if (kit.getContents() != null) {
            for (int i = 0; i < kit.getContents().length; i++) {
                if (kit.getContents()[i] != null) {
                    config.set("contents." + i, kit.getContents()[i]);
                }
            }
        }
        
        if (kit.getArmor() != null) {
            for (int i = 0; i < kit.getArmor().length; i++) {
                if (kit.getArmor()[i] != null) {
                    config.set("armor." + i, kit.getArmor()[i]);
                }
            }
        }
        
        if (kit.getIcon() != null) {
            config.set("icon", kit.getIcon());
        }
        
        // Save kit rules
        KitRules rules = kit.getRules();
        config.set("rules.natural_health_regen", rules.isNaturalHealthRegen());
        config.set("rules.block_break", rules.isBlockBreak());
        config.set("rules.block_place", rules.isBlockPlace());
        config.set("rules.damage_multiplier", rules.getDamageMultiplier());
        config.set("rules.instant_tnt", rules.isInstantTnt());
        
        try {
            config.save(kitFile);
        } catch (IOException e) {
            plugin.getLogger().severe("§c❌ Failed to save kit: " + kit.getName() + " - " + e.getMessage());
        }
    }
    
    public Kit getKit(String name) {
        return kits.get(name);
    }
    
    public Map<String, Kit> getKits() {
        return new HashMap<>(kits);
    }
    
    public void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        
        if (kit.getContents() != null) {
            player.getInventory().setContents(kit.getContents());
        }
        
        if (kit.getArmor() != null) {
            player.getInventory().setArmorContents(kit.getArmor());
        }
        
        player.updateInventory();
    }
}
