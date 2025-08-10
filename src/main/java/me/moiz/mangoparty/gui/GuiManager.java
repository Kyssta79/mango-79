package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import me.moiz.mangoparty.models.Arena;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;

public class GuiManager implements Listener {
    private MangoParty plugin;
    private YamlConfiguration splitConfig;
    private YamlConfiguration ffaConfig;
    
    // Add this field at the top of the class
    private Map<UUID, UUID> challengerTargets = new HashMap<>();
    
    public GuiManager(MangoParty plugin) {
        this.plugin = plugin;
        loadGuiConfigs();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadGuiConfigs() {
        File guiDir = new File(plugin.getDataFolder(), "gui");
        if (!guiDir.exists()) {
            guiDir.mkdirs();
        }
        
        File splitFile = new File(guiDir, "split.yml");
        File ffaFile = new File(guiDir, "ffa.yml");
        
        if (!splitFile.exists()) {
            plugin.saveResource("gui/split.yml", false);
        }
        if (!ffaFile.exists()) {
            plugin.saveResource("gui/ffa.yml", false);
        }
        
        splitConfig = YamlConfiguration.loadConfiguration(splitFile);
        ffaConfig = YamlConfiguration.loadConfiguration(ffaFile);
    }

    // Make this method public so ConfigManager can trigger a reload
    public void reloadGuiConfigs() {
        loadGuiConfigs();
        plugin.getLogger().info("GUI configs reloaded.");
    }
    
    public void openMatchTypeGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Select Match Type");
        
        // Party Split item
        ItemStack splitItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta splitMeta = splitItem.getItemMeta();
        splitMeta.setDisplayName("§aParty Split");
        List<String> splitLore = new ArrayList<>();
        splitLore.add("§7Divide party into teams");
        splitLore.add("§7and fight each other");
        splitMeta.setLore(splitLore);
        splitItem.setItemMeta(splitMeta);
        gui.setItem(10, splitItem);
        
        // Party FFA item
        ItemStack ffaItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta ffaMeta = ffaItem.getItemMeta();
        ffaMeta.setDisplayName("§cParty FFA");
        List<String> ffaLore = new ArrayList<>();
        ffaLore.add("§7Free for all battle");
        ffaLore.add("§7Last player standing wins");
        ffaMeta.setLore(ffaLore);
        ffaItem.setItemMeta(ffaMeta);
        gui.setItem(12, ffaItem);
        
        // Party vs Party item
        ItemStack pvpItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName("§eParty vs Party");
        List<String> pvpLore = new ArrayList<>();
        pvpLore.add("§7Challenge another party");
        pvpLore.add("§7to an epic team battle");
        pvpMeta.setLore(pvpLore);
        pvpItem.setItemMeta(pvpMeta);
        gui.setItem(14, pvpItem);
        
        // Queue modes
        ItemStack queue1v1 = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta queue1v1Meta = queue1v1.getItemMeta();
        queue1v1Meta.setDisplayName("§61v1 Queue");
        List<String> queue1v1Lore = new ArrayList<>();
        queue1v1Lore.add("§7Join 1v1 ranked queue");
        queue1v1Lore.add("§7Fight solo opponents");
        queue1v1Meta.setLore(queue1v1Lore);
        queue1v1.setItemMeta(queue1v1Meta);
        gui.setItem(19, queue1v1);
        
        ItemStack queue2v2 = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta queue2v2Meta = queue2v2.getItemMeta();
        queue2v2Meta.setDisplayName("§62v2 Queue");
        List<String> queue2v2Lore = new ArrayList<>();
        queue2v2Lore.add("§7Join 2v2 team queue");
        queue2v2Lore.add("§7Fight with a teammate");
        queue2v2Meta.setLore(queue2v2Lore);
        queue2v2.setItemMeta(queue2v2Meta);
        gui.setItem(21, queue2v2);
        
        ItemStack queue3v3 = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta queue3v3Meta = queue3v3.getItemMeta();
        queue3v3Meta.setDisplayName("§63v3 Queue");
        List<String> queue3v3Lore = new ArrayList<>();
        queue3v3Lore.add("§7Join 3v3 team queue");
        queue3v3Lore.add("§7Epic team battles");
        queue3v3Meta.setLore(queue3v3Lore);
        queue3v3.setItemMeta(queue3v3Meta);
        gui.setItem(23, queue3v3);
        
        player.openInventory(gui);
    }
    
    public void openPartyDuelGui(Player player) {
        Party playerParty = plugin.getPartyManager().getParty(player);
        if (playerParty == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§6Challenge Party");
        
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 54) break;
            
            Party otherParty = plugin.getPartyManager().getParty(online);
            if (otherParty != null && 
                otherParty.isLeader(online.getUniqueId()) && 
                !otherParty.equals(playerParty) &&
                !otherParty.isInMatch()) {
                
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(online);
                meta.setDisplayName("§e" + online.getName() + "'s Party");
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Members: §f" + otherParty.getSize());
                for (Player member : otherParty.getOnlineMembers()) {
                    if (lore.size() < 8) { // Limit lore size
                        lore.add("§8• §7" + member.getName());
                    }
                }
                lore.add("§aClick to challenge!");
                meta.setLore(lore);
                head.setItemMeta(meta);
                
                gui.setItem(slot, head);
                slot++;
            }
        }
        
        player.openInventory(gui);
    }
    
    public void openKitGui(Player player, String matchType) {
        YamlConfiguration config = "split".equalsIgnoreCase(matchType) ? splitConfig : ffaConfig;
        String title = config.getString("title", "§6Select Kit");
        int size = config.getInt("size", 27);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            Map<String, Kit> availableKits = plugin.getKitManager().getKits();
            
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null) {
                    String kitName = kitSection.getString("kit");
                    Kit kit = availableKits.get(kitName);
                    
                    if (kit != null) {
                        int slot = kitSection.getInt("slot");
                        String displayName = kitSection.getString("name", kit.getDisplayName());
                        List<String> lore = kitSection.getStringList("lore");
                        
                        ItemStack item = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.IRON_SWORD);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        
                        if (kitSection.contains("customModelData")) {
                            meta.setCustomModelData(kitSection.getInt("customModelData"));
                        }
                        
                        item.setItemMeta(meta);
                        gui.setItem(slot, item);
                    } else {
                        plugin.getLogger().warning("Kit '" + kitName + "' defined in " + matchType + ".yml but not found in KitManager.");
                    }
                }
            }
        }
        
        player.openInventory(gui);
    }
    
    public void openQueueKitGui(Player player, String mode) {
        YamlConfiguration config = loadQueueConfig(mode);
        String title = "§6" + mode.toUpperCase() + " Kit Selection";
        int size = 27; // Default size
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            Map<String, Kit> availableKits = plugin.getKitManager().getKits();
            
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null) {
                    String kitName = kitKey; // Use the key as kit name
                    Kit kit = availableKits.get(kitName);
                    
                    if (kit != null) {
                        int slot = kitSection.getInt("slot");
                        String materialName = kitSection.getString("material", "IRON_SWORD");
                        String displayName = kitSection.getString("name", kit.getDisplayName());
                        List<String> lore = new ArrayList<>(kitSection.getStringList("lore"));
                        
                        // Replace {queued} placeholder
                        int queueCount = plugin.getQueueManager().getQueueCount(mode, kitName);
                        for (int i = 0; i < lore.size(); i++) {
                            lore.set(i, lore.get(i).replace("{queued}", String.valueOf(queueCount)));
                        }
                        
                        Material material = Material.valueOf(materialName);
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        
                        if (kitSection.contains("customModelData")) {
                            meta.setCustomModelData(kitSection.getInt("customModelData"));
                        }
                        
                        item.setItemMeta(meta);
                        gui.setItem(slot, item);
                    }
                }
            }
        }
        
        player.openInventory(gui);
    }

    public void openPartyVsPartyKitGui(Player challenger, Player challengedLeader) {
        // Use split config for party vs party
        String title = "§6Select Kit for Party Duel";
        int size = splitConfig.getInt("size", 27);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        ConfigurationSection kitsSection = splitConfig.getConfigurationSection("kits");
        if (kitsSection != null) {
            Map<String, Kit> availableKits = plugin.getKitManager().getKits();
            
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null) {
                    String kitName = kitSection.getString("kit");
                    Kit kit = availableKits.get(kitName);
                    
                    if (kit != null) {
                        int slot = kitSection.getInt("slot");
                        String displayName = kitSection.getString("name", kit.getDisplayName());
                        List<String> lore = new ArrayList<>(kitSection.getStringList("lore"));
                        lore.add("§7");
                        lore.add("§eClick to challenge with this kit!");
                        
                        ItemStack item = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.IRON_SWORD);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        
                        if (kitSection.contains("customModelData")) {
                            meta.setCustomModelData(kitSection.getInt("customModelData"));
                        }
                        
                        item.setItemMeta(meta);
                        gui.setItem(slot, item);
                    }
                }
            }
        }
        
        // Store the challenged leader for later use
        challengerTargets.put(challenger.getUniqueId(), challengedLeader.getUniqueId());
        
        challenger.openInventory(gui);
    }

    private YamlConfiguration loadQueueConfig(String mode) {
        File guiDir = new File(plugin.getDataFolder(), "gui");
        File configFile = new File(guiDir, mode + "kits.yml");
        
        if (!configFile.exists()) {
            createDefaultQueueConfig(configFile, mode);
        }
        
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void createDefaultQueueConfig(File configFile, String mode) {
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("title", "§6" + mode.toUpperCase() + " Kit Selection");
        config.set("size", 27);
        
        // Add some default kit slots (empty, to be populated by admin)
        config.set("kits", "");
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create " + configFile.getName() + ": " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals("§6Select Match Type")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (clicked.getType() == Material.IRON_SWORD) {
                // Party Split selected
                openKitGui(player, "split");
            } else if (clicked.getType() == Material.DIAMOND_SWORD) {
                // Party FFA selected
                openKitGui(player, "ffa");
            } else if (clicked.getType() == Material.PLAYER_HEAD) {
                // Party vs Party selected
                openPartyDuelGui(player);
            } else if (clicked.getType() == Material.GOLDEN_SWORD) {
                // 1v1 Queue selected
                openQueueKitGui(player, "1v1");
            } else if (clicked.getType() == Material.GOLDEN_AXE) {
                // 2v2 Queue selected
                openQueueKitGui(player, "2v2");
            } else if (clicked.getType() == Material.NETHERITE_SWORD) {
                // 3v3 Queue selected
                openQueueKitGui(player, "3v3");
            }
        } else if (title.equals("§6Challenge Party")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;
            
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta.getOwningPlayer() != null) {
                Player targetLeader = meta.getOwningPlayer().getPlayer();
                if (targetLeader != null && targetLeader.isOnline()) {
                    openPartyVsPartyKitGui(player, targetLeader);
                }
            }
        } else if (title.contains("Kit Selection") || title.contains("Select Kit")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // Handle different types of kit selection
            if (title.contains("1V1") || title.contains("2V2") || title.contains("3V3")) {
                handleQueueKitSelection(player, title, event.getSlot());
            } else if (title.contains("Party Duel")) {
                handlePartyDuelKitSelection(player, event.getSlot());
            } else if (title.contains("Split") || title.contains("FFA")) {
                handleRegularKitSelection(player, title, event.getSlot());
            }
        }
    }
    
    private void handleQueueKitSelection(Player player, String title, int slot) {
        String mode = extractModeFromTitle(title);
        if (mode == null) return;
        
        YamlConfiguration config = loadQueueConfig(mode);
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        
        if (kitsSection != null) {
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null && kitSection.getInt("slot") == slot) {
                    String kitName = kitKey;
                    plugin.getQueueManager().joinQueue(player, mode, kitName);
                    player.closeInventory();
                    return;
                }
            }
        }
    }

    private void handlePartyDuelKitSelection(Player player, int slot) {
        UUID targetId = challengerTargets.remove(player.getUniqueId());
        if (targetId == null) return;
        
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cTarget player is no longer online!");
            player.closeInventory();
            return;
        }
        
        ConfigurationSection kitsSection = splitConfig.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null && kitSection.getInt("slot") == slot) {
                    String kitName = kitSection.getString("kit");
                    plugin.getPartyDuelManager().challengeParty(player, target, kitName);
                    player.closeInventory();
                    return;
                }
            }
        }
    }

    private void handleRegularKitSelection(Player player, String title, int slot) {
        // Existing kit selection logic
        String matchType = title.contains("Split") ? "split" : "ffa";
        YamlConfiguration config = "split".equalsIgnoreCase(matchType) ? splitConfig : ffaConfig;
        
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String kitKey : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitKey);
                if (kitSection != null && kitSection.getInt("slot") == slot) {
                    String kitName = kitSection.getString("kit");
                    Kit kit = plugin.getKitManager().getKit(kitName);
                    
                    if (kit != null) {
                        startMatchPreparation(player, kit, matchType);
                        player.closeInventory();
                        return;
                    }
                }
            }
        }
    }

    private String extractModeFromTitle(String title) {
        if (title.contains("1V1")) return "1v1";
        if (title.contains("2V2")) return "2v2";
        if (title.contains("3V3")) return "3v3";
        return null;
    }
    
    private void startMatchPreparation(Player player, Kit kit, String matchType) {
        // Get player's party
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cYou must be a party leader to start matches!");
            return;
        }
        
        // Get an available arena
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            player.sendMessage("§cNo available arenas! All arenas are currently in use.");
            return;
        }
        
        // Start the match
        plugin.getMatchManager().startMatch(party, arena, kit, matchType);
        player.sendMessage("§aStarting " + matchType + " match with kit: " + kit.getDisplayName());
    }
}
