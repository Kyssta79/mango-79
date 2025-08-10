package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    private MangoParty plugin;
    private Map<String, Kit> kits;
    private File kitsFile;
    private YamlConfiguration kitsConfig;

    public KitManager(MangoParty plugin) {
        this.plugin = plugin;
        this.kits = new HashMap<>();
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        
        loadKits();
    }

    private void loadKits() {
        if (!kitsFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        
        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String kitName : kitsSection.getKeys(false)) {
                Kit kit = loadKitFromConfig(kitName, kitsSection.getConfigurationSection(kitName));
                if (kit != null) {
                    kits.put(kitName, kit);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }

    private Kit loadKitFromConfig(String name, ConfigurationSection section) {
        try {
            Kit kit = new Kit(name);
            
            // Load icon
            if (section.contains("icon")) {
                ConfigurationSection iconSection = section.getConfigurationSection("icon");
                Material material = Material.valueOf(iconSection.getString("material", "IRON_SWORD"));
                kit.setIcon(new ItemStack(material));
            }
            
            // Load items
            if (section.contains("items")) {
                List<Map<?, ?>> itemsList = section.getMapList("items");
                List<ItemStack> items = new ArrayList<>();
                for (Map<?, ?> itemMap : itemsList) {
                    ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
                    items.add(item);
                }
                kit.setItems(items);
            }
            
            // Load armor
            if (section.contains("armor")) {
                List<Map<?, ?>> armorList = section.getMapList("armor");
                List<ItemStack> armor = new ArrayList<>();
                for (Map<?, ?> armorMap : armorList) {
                    ItemStack armorPiece = ItemStack.deserialize((Map<String, Object>) armorMap);
                    armor.add(armorPiece);
                }
                kit.setArmor(armor);
            }
            
            // Load rules
            if (section.contains("rules")) {
                ConfigurationSection rulesSection = section.getConfigurationSection("rules");
                KitRules rules = new KitRules();
                
                rules.setNaturalHealthRegeneration(rulesSection.getBoolean("naturalHealthRegeneration", true));
                rules.setBlockBreaking(rulesSection.getBoolean("blockBreaking", false));
                rules.setBlockPlacing(rulesSection.getBoolean("blockPlacing", false));
                rules.setDamageMultiplier(rulesSection.getDouble("damageMultiplier", 1.0));
                rules.setInstantTnt(rulesSection.getBoolean("instantTnt", false));
                
                kit.setRules(rules);
            }
            
            return kit;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load kit '" + name + "': " + e.getMessage());
            return null;
        }
    }

    public void saveKit(Kit kit) {
        String path = "kits." + kit.getName();
        
        // Save icon
        if (kit.getIcon() != null) {
            kitsConfig.set(path + ".icon.material", kit.getIcon().getType().name());
        }
        
        // Save items
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (ItemStack item : kit.getItems()) {
            itemsList.add(item.serialize());
        }
        kitsConfig.set(path + ".items", itemsList);
        
        // Save armor
        List<Map<String, Object>> armorList = new ArrayList<>();
        for (ItemStack armor : kit.getArmor()) {
            armorList.add(armor.serialize());
        }
        kitsConfig.set(path + ".armor", armorList);
        
        // Save rules
        KitRules rules = kit.getRules();
        kitsConfig.set(path + ".rules.naturalHealthRegeneration", rules.isNaturalHealthRegeneration());
        kitsConfig.set(path + ".rules.blockBreaking", rules.isBlockBreaking());
        kitsConfig.set(path + ".rules.blockPlacing", rules.isBlockPlacing());
        kitsConfig.set(path + ".rules.damageMultiplier", rules.getDamageMultiplier());
        kitsConfig.set(path + ".rules.instantTnt", rules.isInstantTnt());
        
        try {
            kitsConfig.save(kitsFile);
            kits.put(kit.getName(), kit);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kit '" + kit.getName() + "': " + e.getMessage());
        }
    }

    public void deleteKit(String name) {
        kits.remove(name);
        kitsConfig.set("kits." + name, null);
        
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to delete kit '" + name + "': " + e.getMessage());
        }
        
        plugin.getLogger().info("Deleted kit: " + name);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Map<String, Kit> getKits() {
        return new HashMap<>(kits);
    }

    public List<Kit> getAllKits() {
        return new ArrayList<>(kits.values());
    }

    public Set<String> getKitNames() {
        return new HashSet<>(kits.keySet());
    }

    public void cleanup() {
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kits on cleanup: " + e.getMessage());
        }
    }
}
