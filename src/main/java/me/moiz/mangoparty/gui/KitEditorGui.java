package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.config.ConfigManager;
import me.moiz.mangoparty.managers.KitManager;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitEditorGui implements Listener {
    private MangoParty plugin;
    private KitManager kitManager;
    private ConfigManager configManager;
    private Map<UUID, String> editingKit;
    private Map<UUID, String> waitingForSlotInput;
    private Map<UUID, String> slotInputType;
    private Map<UUID, Long> inputTimeout;

    public KitEditorGui(MangoParty plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        this.configManager = plugin.getConfigManager();
        this.editingKit = new HashMap<>();
        this.waitingForSlotInput = new HashMap<>();
        this.slotInputType = new HashMap<>();
        this.inputTimeout = new HashMap<>();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openKitListGui(Player player) {
        String title = HexUtils.colorize("&6Kit Manager");
        int size = 54;
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        Map<String, Kit> kits = kitManager.getKits();
        int slot = 0;
        
        for (Kit kit : kits.values()) {
            if (slot >= size) break;
            
            ItemStack item = createKitItem(kit);
            gui.setItem(slot, item);
            slot++;
        }
        
        player.openInventory(gui);
    }

    private ItemStack createKitItem(Kit kit) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(HexUtils.colorize("&e" + kit.getName()));
        
        List<String> lore = new ArrayList<>();
        lore.add(HexUtils.colorize("&7Click to edit"));
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }

    public void openKitEditor(Player player, String kitName) {
        Kit kit = kitManager.getKit(kitName);
        if (kit == null) {
            player.sendMessage(HexUtils.colorize("&cKit not found!"));
            return;
        }

        editingKit.put(player.getUniqueId(), kitName);

        File configFile = new File(plugin.getDataFolder(), "gui/kit_editor.yml");
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        String title = HexUtils.colorize(config.getString("title", "&6Kit Editor").replace("{kit}", kitName));
        int size = config.getInt("size", 27);
        
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Load items from config
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                ItemStack item = createItemFromConfig(itemSection, kit);
                if (item != null) {
                    int slot = itemSection.getInt("slot", 0);
                    gui.setItem(slot, item);
                }
            }
        }

        player.openInventory(gui);
    }

    private void createDefaultConfig(File configFile) {
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("title", "&6Kit Editor - {kit}");
        config.set("size", 27);
        
        // Edit GUI Slots
        config.set("items.edit_gui_slots.slot", 10);
        config.set("items.edit_gui_slots.material", "CHEST");
        config.set("items.edit_gui_slots.name", "&eEdit GUI Slots");
        config.set("items.edit_gui_slots.lore", List.of(
            "&7Click to edit kit positions in GUIs",
            "&7",
            "&eCurrent Positions:",
            "&7Split: &f{split_slot}",
            "&7FFA: &f{ffa_slot}",
            "&71v1: &f{1v1_slot}",
            "&72v2: &f{2v2_slot}",
            "&73v3: &f{3v3_slot}"
        ));
        config.set("items.edit_gui_slots.action", "EDIT_GUI_SLOTS");
        
        // Set Icon
        config.set("items.set_icon.slot", 11);
        config.set("items.set_icon.material", "ITEM_FRAME");
        config.set("items.set_icon.name", "&bSet Icon");
        config.set("items.set_icon.lore", List.of("&7Click to set kit icon", "&7Hold the item you want as icon"));
        config.set("items.set_icon.action", "SET_ICON");
        
        // Save Kit
        config.set("items.save_kit.slot", 15);
        config.set("items.save_kit.material", "EMERALD");
        config.set("items.save_kit.name", "&aSave Kit");
        config.set("items.save_kit.lore", List.of("&7Click to save kit changes"));
        config.set("items.save_kit.action", "SAVE_KIT");
        
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create kit_editor.yml: " + e.getMessage());
        }
    }

    private ItemStack createItemFromConfig(ConfigurationSection section, Kit kit) {
        try {
            Material material = Material.valueOf(section.getString("material", "STONE"));
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                String name = section.getString("name", "");
                name = replacePlaceholders(name, kit);
                meta.setDisplayName(HexUtils.colorize(name));
                
                List<String> lore = section.getStringList("lore");
                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(HexUtils.colorize(replacePlaceholders(line, kit)));
                }
                meta.setLore(processedLore);
                
                if (section.contains("customModelData")) {
                    meta.setCustomModelData(section.getInt("customModelData"));
                }
                
                item.setItemMeta(meta);
            }
            
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create item from config: " + e.getMessage());
            return null;
        }
    }

    private String replacePlaceholders(String text, Kit kit) {
        text = text.replace("{kit}", kit.getName());
        text = text.replace("{split_slot}", getSlotText(kit.getName(), "split"));
        text = text.replace("{ffa_slot}", getSlotText(kit.getName(), "ffa"));
        text = text.replace("{1v1_slot}", getSlotText(kit.getName(), "1v1"));
        text = text.replace("{2v2_slot}", getSlotText(kit.getName(), "2v2"));
        text = text.replace("{3v3_slot}", getSlotText(kit.getName(), "3v3"));
        return text;
    }

    private String getSlotText(String kitName, String guiType) {
        Integer slot = configManager.getKitSlotInGui(kitName, guiType);
        return slot != null ? String.valueOf(slot) : "Not in GUI";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.contains("Kit Manager")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            String kitName = extractKitName(clicked);
            if (kitName != null) {
                openKitEditor(player, kitName);
            }
        } else if (title.contains("Kit Editor")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;
            
            String kitName = editingKit.get(player.getUniqueId());
            if (kitName == null) return;
            
            Kit kit = kitManager.getKit(kitName);
            if (kit == null) return;
            
            String action = getActionFromItem(clickedItem);
            if (action == null) return;
            
            handleAction(player, kit, action);
        } else if (title.contains("Edit GUI Slots")) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;
            
            String kitName = editingKit.get(player.getUniqueId());
            if (kitName == null) return;
            
            String guiType = identifyGuiTypeFromSlotEditor(event.getSlot());
            if (guiType != null) {
                handleSlotEditorClick(player, kitName, guiType);
            }
        }
    }

    private String extractKitName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName();
            return HexUtils.stripColor(displayName);
        }
        return null;
    }

    private String getActionFromItem(ItemStack item) {
        File configFile = new File(plugin.getDataFolder(), "gui/kit_editor.yml");
        if (!configFile.exists()) return null;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection.getString("material", "").equals(item.getType().name())) {
                    String displayName = HexUtils.colorize(itemSection.getString("name", ""));
                    if (item.getItemMeta().getDisplayName().contains(displayName.replace("&", "§"))) {
                        return itemSection.getString("action");
                    }
                }
            }
        }
        
        return null;
    }

    private void handleAction(Player player, Kit kit, String action) {
        switch (action) {
            case "EDIT_GUI_SLOTS":
                openSlotEditor(player, kit);
                break;
                
            case "SET_ICON":
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() == Material.AIR) {
                    player.sendMessage(HexUtils.colorize("&cHold an item to set as icon!"));
                    return;
                }
                
                kit.setIcon(heldItem.clone());
                kitManager.saveKit(kit);
                configManager.updateKitIconInAllGuis(kit);
                player.sendMessage(HexUtils.colorize("&aKit icon updated!"));
                reopenGui(player, kit.getName());
                break;
                
            case "SAVE_KIT":
                kitManager.saveKit(kit);
                player.sendMessage(HexUtils.colorize("&aKit saved successfully!"));
                break;
        }
    }

    private void openSlotEditor(Player player, Kit kit) {
        player.closeInventory();
        
        Inventory slotGui = Bukkit.createInventory(null, 27, HexUtils.colorize("&6Edit GUI Slots - " + kit.getName()));
        
        // Split GUI
        ItemStack splitItem = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta splitMeta = splitItem.getItemMeta();
        splitMeta.setDisplayName(HexUtils.colorize("&6Split GUI"));
        splitMeta.setLore(List.of(
            HexUtils.colorize("&7Click to edit slot in Split GUI"),
            HexUtils.colorize("&7Current slot: &f" + getSlotText(kit.getName(), "split"))
        ));
        splitItem.setItemMeta(splitMeta);
        slotGui.setItem(10, splitItem);
        
        // FFA GUI
        ItemStack ffaItem = new ItemStack(Material.RED_WOOL);
        ItemMeta ffaMeta = ffaItem.getItemMeta();
        ffaMeta.setDisplayName(HexUtils.colorize("&cFFA GUI"));
        ffaMeta.setLore(List.of(
            HexUtils.colorize("&7Click to edit slot in FFA GUI"),
            HexUtils.colorize("&7Current slot: &f" + getSlotText(kit.getName(), "ffa"))
        ));
        ffaItem.setItemMeta(ffaMeta);
        slotGui.setItem(11, ffaItem);
        
        // 1v1 Queue
        ItemStack oneVoneItem = new ItemStack(Material.BLUE_WOOL);
        ItemMeta oneVoneMeta = oneVoneItem.getItemMeta();
        oneVoneMeta.setDisplayName(HexUtils.colorize("&91v1 Queue"));
        oneVoneMeta.setLore(List.of(
            HexUtils.colorize("&7Click to edit slot in 1v1 Queue"),
            HexUtils.colorize("&7Current slot: &f" + getSlotText(kit.getName(), "1v1"))
        ));
        oneVoneItem.setItemMeta(oneVoneMeta);
        slotGui.setItem(12, oneVoneItem);
        
        // 2v2 Queue
        ItemStack twoVtwoItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta twoVtwoMeta = twoVtwoItem.getItemMeta();
        twoVtwoMeta.setDisplayName(HexUtils.colorize("&a2v2 Queue"));
        twoVtwoMeta.setLore(List.of(
            HexUtils.colorize("&7Click to edit slot in 2v2 Queue"),
            HexUtils.colorize("&7Current slot: &f" + getSlotText(kit.getName(), "2v2"))
        ));
        twoVtwoItem.setItemMeta(twoVtwoMeta);
        slotGui.setItem(13, twoVtwoItem);
        
        // 3v3 Queue
        ItemStack threeVthreeItem = new ItemStack(Material.PURPLE_WOOL);
        ItemMeta threeVthreeMeta = threeVthreeItem.getItemMeta();
        threeVthreeMeta.setDisplayName(HexUtils.colorize("&53v3 Queue"));
        threeVthreeMeta.setLore(List.of(
            HexUtils.colorize("&7Click to edit slot in 3v3 Queue"),
            HexUtils.colorize("&7Current slot: &f" + getSlotText(kit.getName(), "3v3"))
        ));
        threeVthreeItem.setItemMeta(threeVthreeMeta);
        slotGui.setItem(14, threeVthreeItem);
        
        player.openInventory(slotGui);
    }

    private String identifyGuiTypeFromSlotEditor(int slot) {
        switch (slot) {
            case 10: return "split";
            case 11: return "ffa";
            case 12: return "1v1";
            case 13: return "2v2";
            case 14: return "3v3";
            default: return null;
        }
    }

    private void handleSlotEditorClick(Player player, String kitName, String guiType) {
        startSlotInput(player, kitName, guiType);
    }

    private void startSlotInput(Player player, String kitName, String guiType) {
        player.closeInventory();
        
        waitingForSlotInput.put(player.getUniqueId(), kitName);
        slotInputType.put(player.getUniqueId(), guiType);
        inputTimeout.put(player.getUniqueId(), System.currentTimeMillis() + 30000);
        
        player.sendMessage("");
        player.sendMessage(HexUtils.colorize("&8&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(HexUtils.colorize("&6&lEDIT SLOT POSITION"));
        player.sendMessage("");
        player.sendMessage(HexUtils.colorize("&7Kit: &e" + kitName));
        player.sendMessage(HexUtils.colorize("&7GUI Type: &e" + guiType.toUpperCase()));
        player.sendMessage(HexUtils.colorize("&7Current slot: &f" + getSlotText(kitName, guiType)));
        player.sendMessage("");
        player.sendMessage(HexUtils.colorize("&eType the new slot number (0-26), or 'cancel' to cancel:"));
        player.sendMessage(HexUtils.colorize("&8&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!waitingForSlotInput.containsKey(playerId)) return;
        
        event.setCancelled(true);
        
        // Check timeout
        if (System.currentTimeMillis() > inputTimeout.get(playerId)) {
            waitingForSlotInput.remove(playerId);
            slotInputType.remove(playerId);
            inputTimeout.remove(playerId);
            player.sendMessage(HexUtils.colorize("&cInput timed out!"));
            return;
        }
        
        String message = event.getMessage().trim();
        String kitName = waitingForSlotInput.get(playerId);
        String guiType = slotInputType.get(playerId);
        
        // Clean up
        waitingForSlotInput.remove(playerId);
        slotInputType.remove(playerId);
        inputTimeout.remove(playerId);
        
        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(HexUtils.colorize("&cCancelled!"));
            Bukkit.getScheduler().runTask(plugin, () -> reopenGui(player, kitName));
            return;
        }
        
        try {
            int newSlot = Integer.parseInt(message);
            
            if (newSlot < 0 || newSlot > 26) {
                player.sendMessage(HexUtils.colorize("&cSlot must be between 0 and 26!"));
                Bukkit.getScheduler().runTask(plugin, () -> reopenGui(player, kitName));
                return;
            }
            
            // Check if slot is already taken
            String existingKit = configManager.getKitAtSlot(guiType, newSlot);
            if (existingKit != null && !existingKit.equals(kitName)) {
                player.sendMessage(HexUtils.colorize("&cSlot " + newSlot + " is already taken by kit: " + existingKit));
                Bukkit.getScheduler().runTask(plugin, () -> reopenGui(player, kitName));
                return;
            }
            
            // Update slot
            boolean success;
            if (guiType.equals("1v1") || guiType.equals("2v2") || guiType.equals("3v3")) {
                success = configManager.updateKitSlotInQueueGui(kitName, guiType, newSlot);
            } else {
                success = configManager.updateKitSlotInGui(kitName, guiType, newSlot);
            }
            
            if (success) {
                player.sendMessage(HexUtils.colorize("&aSlot updated to " + newSlot + " in " + guiType.toUpperCase() + " GUI!"));
            } else {
                player.sendMessage(HexUtils.colorize("&cFailed to update slot!"));
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage(HexUtils.colorize("&cInvalid number! Please enter a valid slot number."));
        }
        
        Bukkit.getScheduler().runTask(plugin, () -> reopenGui(player, kitName));
    }

    public void reloadConfigs() {
        // Reload configurations
    }

    private void reopenGui(Player player, String kitName) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openKitEditor(player, kitName);
        }, 1L);
    }
}
