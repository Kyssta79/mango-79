package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits");
    }

    private Kit loadKitFromFile(String name, File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Kit kit = new Kit(name);
            
            kit.setDisplayName(config.getString("displayName", name));
            
            // Load contents (items)
            if (config.contains("contents")) {
                List<ItemStack> items = new ArrayList<>();
                for (int i = 0; i < 36; i++) {
                    if (config.contains("contents." + i)) {
                        ItemStack item = config.getItemStack("contents." + i);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                }
                kit.setItems(items);
            }
            
            // Load armor
            if (config.contains("armor")) {
                List<ItemStack> armor = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    if (config.contains("armor." + i)) {
                        ItemStack armorPiece = config.getItemStack("armor." + i);
                        if (armorPiece != null) {
                            armor.add(armorPiece);
                        }
                    }
                }
                kit.setArmor(armor);
            }
            
            // Load icon
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
            plugin.getLogger().warning("Failed to load kit: " + name + " - " + e.getMessage());
            return null;
        }
    }

    public void createKit(String name, Player player) {
        Kit kit = new Kit(name);
        kit.setDisplayName(name);
        
        // Convert player inventory to lists
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                items.add(item.clone());
            }
        }
        kit.setItems(items);
        
        List<ItemStack> armor = new ArrayList<>();
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null) {
                armor.add(armorPiece.clone());
            }
        }
        kit.setArmor(armor);
        
        // Use first item in inventory as icon, or default to sword
        ItemStack icon = null;
        for (ItemStack item : items) {
            if (item != null) {
                icon = item.clone();
                icon.setAmount(1);
                break;
            }
        }
        if (icon == null) {
            icon = new ItemStack(Material.IRON_SWORD);
        }
        kit.setIcon(icon);
        
        kits.put(name, kit);
        saveKit(kit);
        plugin.getLogger().info("Created new kit: " + name + " by " + player.getName());
    }

    public void saveKit(Kit kit) {
        File kitFile = new File(kitsDir, kit.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("displayName", kit.getDisplayName());
        
        // Save items
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                config.set("contents." + i, items.get(i));
            }
        }
        
        // Save armor
        List<ItemStack> armor = kit.getArmor();
        for (int i = 0; i < armor.size(); i++) {
            if (armor.get(i) != null) {
                config.set("armor." + i, armor.get(i));
            }
        }
        
        // Save icon
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
            plugin.getLogger().severe("Failed to save kit: " + kit.getName() + " - " + e.getMessage());
        }
    }

    public void giveKit(Player player, Kit kit) {
        player.getInventory().clear();
        
        // Give items
        List<ItemStack> items = kit.getItems();
        for (int i = 0; i < items.size() && i < 36; i++) {
            if (items.get(i) != null) {
                player.getInventory().setItem(i, items.get(i).clone());
            }
        }
        
        // Give armor
        List<ItemStack> armor = kit.getArmor();
        ItemStack[] armorArray = new ItemStack[4];
        for (int i = 0; i < armor.size() && i < 4; i++) {
            if (armor.get(i) != null) {
                armorArray[i] = armor.get(i).clone();
            }
        }
        player.getInventory().setArmorContents(armorArray);
        
        player.updateInventory();
    }

    public void deleteKit(String name) {
        kits.remove(name);
        File kitFile = new File(kitsDir, name + ".yml");
        if (kitFile.exists()) {
            kitFile.delete();
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
        // Save all kits on cleanup
        for (Kit kit : kits.values()) {
            saveKit(kit);
        }
    }
}
