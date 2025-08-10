package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitEditorGui implements Listener {
    private MangoParty plugin;
    private YamlConfiguration guiConfig;
    private Map<UUID, String> awaitingInput = new HashMap<>();
    private Map<UUID, Kit> editingKit = new HashMap<>();

    public KitEditorGui(MangoParty plugin) {
        this.plugin = plugin;
        loadGuiConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "gui/kit_editor.yml");
        if (!guiFile.exists()) {
            createDefaultKitEditorConfig(guiFile);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    private void createDefaultKitEditorConfig(File file) {
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("kit_list.title", "&6Kit Manager");
        config.set("kit_list.size", 54);
        config.set("kit_list.items.create.material", "EMERALD");
        config.set("kit_list.items.create.name", "&a&lCreate New Kit");
        config.set("kit_list.items.create.slot", 0);
        config.set("kit_list.items.start_slot", 9);
        
        config.set("title", "&6Kit Editor: {kit}");
        config.set("size", 54);
        config.set("items.icon.slot", 4);
        config.set("items.items.slot", 20);
        config.set("items.armor.slot", 22);
        config.set("items.rules.slot", 24);
        config.set("items.save.slot", 40);
        config.set("items.back.slot", 49);
        
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create kit_editor.yml: " + e.getMessage());
        }
    }

    public void openKitListGui(Player player) {
        String title = HexUtils.colorize(guiConfig.getString("kit_list.title", "&6Kit Manager"));
        int size = guiConfig.getInt("kit_list.size", 54);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        // Add create new kit button
        ItemStack createButton = createItem(
            Material.valueOf(guiConfig.getString("kit_list.items.create.material", "EMERALD")),
            HexUtils.colorize(guiConfig.getString("kit_list.items.create.name", "&a&lCreate New Kit")),
            Arrays.asList(HexUtils.colorize("&7Click to create a new kit"))
        );
        gui.setItem(guiConfig.getInt("kit_list.items.create.slot", 0), createButton);
        
        // Add existing kits
        int slot = guiConfig.getInt("kit_list.items.start_slot", 9);
        Map<String, Kit> kits = plugin.getKitManager().getKits();
        
        for (Map.Entry<String, Kit> entry : kits.entrySet()) {
            if (slot >= size) break;
            
            String kitName = entry.getKey();
            Kit kit = entry.getValue();
            
            ItemStack kitItem = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = kitItem.getItemMeta();
            meta.setDisplayName(HexUtils.colorize("&e" + kitName));
            
            List<String> lore = new ArrayList<>();
            lore.add(HexUtils.colorize("&7Items: &f" + kit.getItems().size()));
            lore.add(HexUtils.colorize("&7Armor: &f" + kit.getArmor().size()));
            lore.add("");
            lore.add(HexUtils.colorize("&aLeft-click to edit"));
            lore.add(HexUtils.colorize("&cRight-click to delete"));
            
            meta.setLore(lore);
            kitItem.setItemMeta(meta);
            gui.setItem(slot, kitItem);
            slot++;
        }
        
        player.openInventory(gui);
    }

    public void openKitEditorGui(Player player, String kitName) {
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) {
            player.sendMessage(HexUtils.colorize("&cKit not found!"));
            return;
        }
        
        openKitEditor(player, kit);
    }

    private void openKitEditor(Player player, Kit kit) {
        String title = HexUtils.colorize(guiConfig.getString("title", "&6Kit Editor: {kit}").replace("{kit}", kit.getName()));
        int size = guiConfig.getInt("size", 54);
        
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Fill with glass panes
        ItemStack glassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Kit icon
        ItemStack iconItem = createKitIconItem(kit);
        inventory.setItem(guiConfig.getInt("items.icon.slot", 4), iconItem);
        
        // Kit items
        ItemStack itemsItem = createKitItemsItem(kit);
        inventory.setItem(guiConfig.getInt("items.items.slot", 20), itemsItem);
        
        // Kit armor
        ItemStack armorItem = createKitArmorItem(kit);
        inventory.setItem(guiConfig.getInt("items.armor.slot", 22), armorItem);
        
        // Kit rules
        ItemStack rulesItem = createKitRulesItem(kit);
        inventory.setItem(guiConfig.getInt("items.rules.slot", 24), rulesItem);
        
        // Save kit
        ItemStack saveItem = createSaveItem();
        inventory.setItem(guiConfig.getInt("items.save.slot", 40), saveItem);
        
        // Back button
        ItemStack backItem = createBackItem();
        inventory.setItem(guiConfig.getInt("items.back.slot", 49), backItem);
        
        player.openInventory(inventory);
    }

    private ItemStack createKitIconItem(Kit kit) {
        ItemStack icon = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(HexUtils.colorize("&6Kit Icon"));
        
        List<String> lore = new ArrayList<>();
        lore.add(HexUtils.colorize("&7Current icon: &f" + icon.getType().name()));
        lore.add("");
        lore.add(HexUtils.colorize("&7Hold an item and click to set"));
        lore.add(HexUtils.colorize("&7as the kit icon"));
        
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack createKitItemsItem(Kit kit) {
        ItemStack item = createItem(Material.CHEST, 
            HexUtils.colorize("&6Kit Items"), 
            Arrays.asList(
                HexUtils.colorize("&7Items: &f" + kit.getItems().size()),
                "",
                HexUtils.colorize("&7Click to edit kit items")
            )
        );
        return item;
    }

    private ItemStack createKitArmorItem(Kit kit) {
        ItemStack item = createItem(Material.DIAMOND_CHESTPLATE, 
            HexUtils.colorize("&6Kit Armor"), 
            Arrays.asList(
                HexUtils.colorize("&7Armor pieces: &f" + kit.getArmor().size()),
                "",
                HexUtils.colorize("&7Click to edit kit armor")
            )
        );
        return item;
    }

    private ItemStack createKitRulesItem(Kit kit) {
        KitRules rules = kit.getRules();
        
        ItemStack item = createItem(Material.BOOK, 
            HexUtils.colorize("&6Kit Rules"), 
            Arrays.asList(
                HexUtils.colorize("&7Health Regen: " + (rules.isNaturalHealthRegen() ? "&aEnabled" : "&cDisabled")),
                HexUtils.colorize("&7Block Breaking: " + (rules.isBlockBreak() ? "&aEnabled" : "&cDisabled")),
                HexUtils.colorize("&7Block Placing: " + (rules.isBlockPlace() ? "&aEnabled" : "&cDisabled")),
                HexUtils.colorize("&7Damage Multiplier: &f" + rules.getDamageMultiplier()),
                HexUtils.colorize("&7Instant TNT: " + (rules.isInstantTnt() ? "&aEnabled" : "&cDisabled")),
                "",
                HexUtils.colorize("&7Click to edit rules")
            )
        );
        return item;
    }

    private ItemStack createSaveItem() {
        return createItem(Material.EMERALD, 
            HexUtils.colorize("&aSave Kit"), 
            Arrays.asList(HexUtils.colorize("&7Click to save changes"))
        );
    }

    private ItemStack createBackItem() {
        return createItem(Material.ARROW, 
            HexUtils.colorize("&cBack"), 
            Arrays.asList(HexUtils.colorize("&7Return to kit list"))
        );
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        
        // Handle kit list GUI
        if (title.equals(HexUtils.colorize(guiConfig.getString("kit_list.title", "&6Kit Manager")))) {
            event.setCancelled(true);
            handleKitListClick(player, event);
            return;
        }
        
        // Handle kit editor GUI
        if (title.startsWith(HexUtils.colorize(guiConfig.getString("title", "&6Kit Editor:").split("\\{")[0]))) {
            event.setCancelled(true);
            handleKitEditorClick(player, event);
            return;
        }
        
        // Handle kit rules GUI
        if (title.startsWith("Kit Rules:")) {
            event.setCancelled(true);
            handleKitRulesClick(player, event);
            return;
        }
    }

    private void handleKitListClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.EMERALD) {
            // Create new kit
            player.closeInventory();
            player.sendMessage(HexUtils.colorize("&aType the name of the new kit in chat:"));
            awaitingInput.put(player.getUniqueId(), "create_kit");
            return;
        }
        
        String kitName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        
        if (event.isLeftClick()) {
            // Edit kit
            openKitEditorGui(player, kitName);
        } else if (event.isRightClick()) {
            // Delete kit
            plugin.getKitManager().deleteKit(kitName);
            player.sendMessage(HexUtils.colorize("&cDeleted kit: " + kitName));
            openKitListGui(player); // Refresh
        }
    }

    private void handleKitEditorClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        String kitName = getKitNameFromTitle(event.getView().getTitle());
        Kit kit = plugin.getKitManager().getKit(kitName);
        
        if (kit == null) {
            player.sendMessage(HexUtils.colorize("&cKit not found!"));
            player.closeInventory();
            return;
        }
        
        if (displayName.contains("Kit Icon")) {
            // Set kit icon
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem != null && heldItem.getType() != Material.AIR) {
                kit.setIcon(heldItem.clone());
                plugin.getKitManager().saveKit(kit);
                player.sendMessage(HexUtils.colorize("&aKit icon updated!"));
                openKitEditor(player, kit);
            } else {
                player.sendMessage(HexUtils.colorize("&cHold an item to set as icon!"));
            }
        } else if (displayName.contains("Kit Items")) {
            // Open items editor
            openKitItemsEditor(player, kit);
        } else if (displayName.contains("Kit Armor")) {
            // Open armor editor
            openKitArmorEditor(player, kit);
        } else if (displayName.contains("Kit Rules")) {
            // Open rules editor
            openKitRulesEditor(player, kit);
        } else if (displayName.contains("Save Kit")) {
            // Save kit
            plugin.getKitManager().saveKit(kit);
            player.sendMessage(HexUtils.colorize("&aKit saved successfully!"));
        } else if (displayName.contains("Back")) {
            // Back to kit list
            openKitListGui(player);
        }
    }

    private void openKitRulesEditor(Player player, Kit kit) {
        String title = "Kit Rules: " + kit.getName();
        Inventory inventory = Bukkit.createInventory(null, 27, title);
        
        KitRules rules = kit.getRules();
        
        // Health regeneration
        ItemStack healthRegenItem = createItem(
            rules.isNaturalHealthRegen() ? Material.GOLDEN_APPLE : Material.ROTTEN_FLESH,
            HexUtils.colorize("&6Natural Health Regeneration"),
            Arrays.asList(
                HexUtils.colorize("&7Status: " + (rules.isNaturalHealthRegen() ? "&aEnabled" : "&cDisabled")),
                "",
                HexUtils.colorize("&7Click to toggle")
            )
        );
        inventory.setItem(10, healthRegenItem);
        
        // Block breaking
        ItemStack blockBreakItem = createItem(
            rules.isBlockBreak() ? Material.DIAMOND_PICKAXE : Material.BARRIER,
            HexUtils.colorize("&6Block Breaking"),
            Arrays.asList(
                HexUtils.colorize("&7Status: " + (rules.isBlockBreak() ? "&aEnabled" : "&cDisabled")),
                "",
                HexUtils.colorize("&7Click to toggle")
            )
        );
        inventory.setItem(12, blockBreakItem);
        
        // Block placing
        ItemStack blockPlaceItem = createItem(
            rules.isBlockPlace() ? Material.GRASS_BLOCK : Material.BARRIER,
            HexUtils.colorize("&6Block Placing"),
            Arrays.asList(
                HexUtils.colorize("&7Status: " + (rules.isBlockPlace() ? "&aEnabled" : "&cDisabled")),
                "",
                HexUtils.colorize("&7Click to toggle")
            )
        );
        inventory.setItem(14, blockPlaceItem);
        
        // Damage multiplier
        ItemStack damageMultItem = createItem(Material.IRON_SWORD,
            HexUtils.colorize("&6Damage Multiplier"),
            Arrays.asList(
                HexUtils.colorize("&7Current: &f" + rules.getDamageMultiplier()),
                "",
                HexUtils.colorize("&7Click to change")
            )
        );
        inventory.setItem(16, damageMultItem);
        
        // Instant TNT
        ItemStack instantTntItem = createItem(
            rules.isInstantTnt() ? Material.TNT : Material.BARRIER,
            HexUtils.colorize("&6Instant TNT"),
            Arrays.asList(
                HexUtils.colorize("&7Status: " + (rules.isInstantTnt() ? "&aEnabled" : "&cDisabled")),
                "",
                HexUtils.colorize("&7Click to toggle")
            )
        );
        inventory.setItem(22, instantTntItem);
        
        // Back button
        ItemStack backItem = createItem(Material.ARROW,
            HexUtils.colorize("&cBack"),
            Arrays.asList(HexUtils.colorize("&7Return to kit editor"))
        );
        inventory.setItem(26, backItem);
        
        player.openInventory(inventory);
    }

    private void handleKitRulesClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        String kitName = getKitNameFromTitle(event.getView().getTitle());
        Kit kit = plugin.getKitManager().getKit(kitName);
        
        if (kit == null) {
            player.sendMessage(HexUtils.colorize("&cKit not found!"));
            player.closeInventory();
            return;
        }
        
        KitRules rules = kit.getRules();
        
        if (displayName.contains("Natural Health Regeneration")) {
            rules.setNaturalHealthRegen(!rules.isNaturalHealthRegen());
            player.sendMessage(HexUtils.colorize("&aHealth regeneration " + 
                (rules.isNaturalHealthRegen() ? "enabled" : "disabled")));
            openKitRulesEditor(player, kit);
        } else if (displayName.contains("Block Breaking")) {
            rules.setBlockBreak(!rules.isBlockBreak());
            player.sendMessage(HexUtils.colorize("&aBlock breaking " + 
                (rules.isBlockBreak() ? "enabled" : "disabled")));
            openKitRulesEditor(player, kit);
        } else if (displayName.contains("Block Placing")) {
            rules.setBlockPlace(!rules.isBlockPlace());
            player.sendMessage(HexUtils.colorize("&aBlock placing " + 
                (rules.isBlockPlace() ? "enabled" : "disabled")));
            openKitRulesEditor(player, kit);
        } else if (displayName.contains("Damage Multiplier")) {
            player.closeInventory();
            player.sendMessage(HexUtils.colorize("&aEnter the new damage multiplier (e.g., 1.0, 1.5, 2.0):"));
            awaitingInput.put(player.getUniqueId(), "damage_multiplier");
            editingKit.put(player.getUniqueId(), kit);
        } else if (displayName.contains("Instant TNT")) {
            rules.setInstantTnt(!rules.isInstantTnt());
            player.sendMessage(HexUtils.colorize("&aInstant TNT " + 
                (rules.isInstantTnt() ? "enabled" : "disabled")));
            openKitRulesEditor(player, kit);
        } else if (displayName.contains("Back")) {
            openKitEditor(player, kit);
        }
        
        // Save the kit after any changes
        plugin.getKitManager().saveKit(kit);
    }

    private void openKitItemsEditor(Player player, Kit kit) {
        // TODO: Implement items editor
        player.sendMessage(HexUtils.colorize("&cItems editor not yet implemented!"));
    }

    private void openKitArmorEditor(Player player, Kit kit) {
        // TODO: Implement armor editor
        player.sendMessage(HexUtils.colorize("&cArmor editor not yet implemented!"));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!awaitingInput.containsKey(playerId)) return;
        
        event.setCancelled(true);
        String input = event.getMessage();
        String inputType = awaitingInput.remove(playerId);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (inputType) {
                case "create_kit":
                    if (plugin.getKitManager().getKit(input) != null) {
                        player.sendMessage(HexUtils.colorize("&cA kit with that name already exists!"));
                        return;
                    }
                    
                    plugin.getKitManager().createKit(input, player);
                    player.sendMessage(HexUtils.colorize("&aCreated new kit: " + input));
                    openKitEditorGui(player, input);
                    break;
                    
                case "damage_multiplier":
                    Kit kit = editingKit.remove(playerId);
                    if (kit == null) {
                        player.sendMessage(HexUtils.colorize("&cError: Kit not found!"));
                        return;
                    }
                    
                    try {
                        double multiplier = Double.parseDouble(input);
                        if (multiplier < 0.1 || multiplier > 10.0) {
                            player.sendMessage(HexUtils.colorize("&cDamage multiplier must be between 0.1 and 10.0!"));
                            return;
                        }
                        
                        kit.getRules().setDamageMultiplier(multiplier);
                        plugin.getKitManager().saveKit(kit);
                        player.sendMessage(HexUtils.colorize("&aDamage multiplier set to: " + multiplier));
                        openKitRulesEditor(player, kit);
                    } catch (NumberFormatException e) {
                        player.sendMessage(HexUtils.colorize("&cInvalid number! Please enter a decimal number."));
                    }
                    break;
            }
        });
    }

    private String getKitNameFromTitle(String title) {
        // Extract kit name from title
        String prefix = HexUtils.colorize(guiConfig.getString("title", "&6Kit Editor:").split("\\{")[0]);
        if (title.startsWith(prefix)) {
            return title.substring(prefix.length()).trim();
        }
        
        // For rules editor
        if (title.startsWith("Kit Rules: ")) {
            return title.substring("Kit Rules: ".length());
        }
        
        return "";
    }

    public void reloadConfigs() {
        loadGuiConfig();
        plugin.getLogger().info("Kit editor configs reloaded.");
    }
}
