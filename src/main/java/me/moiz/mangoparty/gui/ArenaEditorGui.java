package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArenaEditorGui implements Listener {
    private MangoParty plugin;
    private YamlConfiguration guiConfig;

    public ArenaEditorGui(MangoParty plugin) {
        this.plugin = plugin;
        loadGuiConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "gui/arena_editor.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui/arena_editor.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void openArenaListGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, HexUtils.colorize(guiConfig.getString("arena_list.title", "&6Arena Manager")));
        
        // Add create new arena button
        ItemStack createButton = createItem(Material.valueOf(guiConfig.getString("arena_list.items.create.material", "EMERALD")), 
            HexUtils.colorize(guiConfig.getString("arena_list.items.create.name", "&a§lCreate New Arena")), 
            List.of(HexUtils.colorize("&7Click to create a new arena")));
        gui.setItem(guiConfig.getInt("arena_list.items.create.slot", 0), createButton);
        
        // Add existing arenas
        int slot = guiConfig.getInt("arena_list.items.start_slot", 9); // Start from second row
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (slot >= 54) break;
            
            ItemStack arenaItem = createItem(
                arena.isComplete() ? Material.valueOf(guiConfig.getString("arena_list.items.arena.complete_material", "GREEN_CONCRETE")) : Material.valueOf(guiConfig.getString("arena_list.items.arena.incomplete_material", "RED_CONCRETE")), 
                HexUtils.colorize("&e" + arena.getName()), 
                List.of(
                    HexUtils.colorize("&7Status: " + (arena.isComplete() ? "&aComplete" : "&cIncomplete")), 
                    HexUtils.colorize("&7Center: " + formatLocation(arena.getCenter())), 
                    HexUtils.colorize("&7Spawn 1: " + formatLocation(arena.getSpawn1())), 
                    HexUtils.colorize("&7Spawn 2: " + formatLocation(arena.getSpawn2())), 
                    HexUtils.colorize("&7Corner 1: " + formatLocation(arena.getCorner1())), 
                    HexUtils.colorize("&7Corner 2: " + formatLocation(arena.getCorner2())), 
                    HexUtils.colorize("&7Allowed Kits: &f" + arena.getAllowedKits().size()), 
                    "", 
                    HexUtils.colorize("&aLeft-click to edit"), 
                    HexUtils.colorize("&cRight-click to delete")
                )
            );
            gui.setItem(slot, arenaItem);
            slot++;
        }
        
        player.openInventory(gui);
    }
    
    private String formatLocation(Location loc) {
        if (loc == null) return HexUtils.colorize("&cNot set");
        return String.format(HexUtils.colorize("&f%.1f, %.1f, %.1f"), loc.getX(), loc.getY(), loc.getZ());
    }

    public void openArenaEditorGui(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage(HexUtils.colorize("&cArena not found!"));
            return;
        }
        
        openArenaEditor(player, arena);
    }

    public void openArenaEditor(Player player, Arena arena) {
        String title = HexUtils.colorize(guiConfig.getString("title", "&6Arena Editor"));
        int size = guiConfig.getInt("size", 54);
        
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
        // Fill with glass panes
        ItemStack glassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Set center location
        ItemStack centerItem = createLocationItem("center", arena.getCenter() != null);
        inventory.setItem(guiConfig.getInt("items.center.slot", 10), centerItem);
        
        // Set spawn locations
        ItemStack spawn1Item = createLocationItem("spawn1", arena.getSpawn1() != null);
        inventory.setItem(guiConfig.getInt("items.spawn1.slot", 12), spawn1Item);
        
        ItemStack spawn2Item = createLocationItem("spawn2", arena.getSpawn2() != null);
        inventory.setItem(guiConfig.getInt("items.spawn2.slot", 14), spawn2Item);
        
        // Set corner locations
        ItemStack corner1Item = createLocationItem("corner1", arena.getCorner1() != null);
        inventory.setItem(guiConfig.getInt("items.corner1.slot", 28), corner1Item);
        
        ItemStack corner2Item = createLocationItem("corner2", arena.getCorner2() != null);
        inventory.setItem(guiConfig.getInt("items.corner2.slot", 34), corner2Item);
        
        // Allowed kits button
        ItemStack allowedKitsItem = createAllowedKitsItem(arena);
        inventory.setItem(guiConfig.getInt("items.allowed_kits.slot", 16), allowedKitsItem);
        
        // Save schematic button
        ItemStack saveSchematicItem = createSaveSchematicItem(arena);
        inventory.setItem(guiConfig.getInt("items.save_schematic.slot", 40), saveSchematicItem);
        
        // Back button
        ItemStack backItem = createBackItem();
        inventory.setItem(guiConfig.getInt("items.back.slot", 49), backItem);
        
        player.openInventory(inventory);
    }

    private ItemStack createLocationItem(String locationType, boolean isSet) {
        ConfigurationSection itemConfig = guiConfig.getConfigurationSection("items." + locationType);
        if (itemConfig == null) return new ItemStack(Material.STONE);
        
        Material material = Material.valueOf(itemConfig.getString("material", "STONE"));
        String name = HexUtils.colorize(itemConfig.getString("name", "&f" + locationType));
        
        List<String> lore = new ArrayList<>();
        if (isSet) {
            lore.add(HexUtils.colorize("&aSet"));
            lore.add(HexUtils.colorize("&7Click to update"));
        } else {
            lore.add(HexUtils.colorize("&cNot set"));
            lore.add(HexUtils.colorize("&7Click to set"));
        }
        
        return createItem(material, name, lore);
    }

    private ItemStack createAllowedKitsItem(Arena arena) {
        ConfigurationSection itemConfig = guiConfig.getConfigurationSection("items.allowed_kits");
        if (itemConfig == null) return new ItemStack(Material.CHEST);
        
        Material material = Material.valueOf(itemConfig.getString("material", "CHEST"));
        String name = HexUtils.colorize(itemConfig.getString("name", "&6Allowed Kits"));
        
        List<String> lore = new ArrayList<>();
        lore.add(HexUtils.colorize("&7Manage which kits are allowed"));
        lore.add(HexUtils.colorize("&7in this arena"));
        lore.add("");
        
        if (arena.getAllowedKits().isEmpty()) {
            lore.add(HexUtils.colorize("&cAll kits disabled"));
        } else {
            lore.add(HexUtils.colorize("&aEnabled kits: &f" + arena.getAllowedKits().size()));
        }
        
        lore.add(HexUtils.colorize("&7Click to manage"));
        
        return createItem(material, name, lore);
    }

    private ItemStack createSaveSchematicItem(Arena arena) {
        ConfigurationSection itemConfig = guiConfig.getConfigurationSection("items.save_schematic");
        if (itemConfig == null) return new ItemStack(Material.PAPER);
        
        Material material = Material.valueOf(itemConfig.getString("material", "PAPER"));
        String name = HexUtils.colorize(itemConfig.getString("name", "&eSave Schematic"));
        
        List<String> lore = new ArrayList<>();
        if (arena.isComplete()) {
            lore.add(HexUtils.colorize("&7Save the arena as a schematic"));
            lore.add(HexUtils.colorize("&7for cloning and regeneration"));
            lore.add("");
            lore.add(HexUtils.colorize("&aClick to save"));
        } else {
            lore.add(HexUtils.colorize("&cArena is incomplete"));
            lore.add(HexUtils.colorize("&7Set all locations first"));
        }
        
        return createItem(material, name, lore);
    }

    private ItemStack createBackItem() {
        ConfigurationSection itemConfig = guiConfig.getConfigurationSection("items.back");
        if (itemConfig == null) return new ItemStack(Material.ARROW);
        
        Material material = Material.valueOf(itemConfig.getString("material", "ARROW"));
        String name = HexUtils.colorize(itemConfig.getString("name", "&cBack"));
        List<String> lore = new ArrayList<>();
        lore.add(HexUtils.colorize("&7Return to arena list"));
        
        return createItem(material, name, lore);
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
        
        String title = HexUtils.colorize(guiConfig.getString("title", "&6Arena Editor"));
        if (!event.getView().getTitle().equals(title)) return;
        
        event.setCancelled(true);
        
        int slot = event.getSlot();
        String arenaName = getArenaNameFromTitle(event.getView().getTitle());
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        
        if (arena == null) {
            player.sendMessage(HexUtils.colorize("&cArena not found!"));
            player.closeInventory();
            return;
        }
        
        // Handle location setting
        if (slot == guiConfig.getInt("items.center.slot", 10)) {
            arena.setCenter(player.getLocation());
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage(HexUtils.colorize("&aCenter location set!"));
            openArenaEditor(player, arena);
        } else if (slot == guiConfig.getInt("items.spawn1.slot", 12)) {
            arena.setSpawn1(player.getLocation());
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage(HexUtils.colorize("&aSpawn 1 location set!"));
            openArenaEditor(player, arena);
        } else if (slot == guiConfig.getInt("items.spawn2.slot", 14)) {
            arena.setSpawn2(player.getLocation());
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage(HexUtils.colorize("&aSpawn 2 location set!"));
            openArenaEditor(player, arena);
        } else if (slot == guiConfig.getInt("items.corner1.slot", 28)) {
            arena.setCorner1(player.getLocation());
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage(HexUtils.colorize("&aCorner 1 location set!"));
            openArenaEditor(player, arena);
        } else if (slot == guiConfig.getInt("items.corner2.slot", 34)) {
            arena.setCorner2(player.getLocation());
            plugin.getArenaManager().saveArena(arena);
            player.sendMessage(HexUtils.colorize("&aCorner 2 location set!"));
            openArenaEditor(player, arena);
        } else if (slot == guiConfig.getInt("items.allowed_kits.slot", 16)) {
            // Open allowed kits GUI
            openAllowedKitsGui(player, arena);
        } else if (slot == guiConfig.getInt("items.save_schematic.slot", 40)) {
            if (arena.isComplete()) {
                player.closeInventory();
                player.sendMessage(HexUtils.colorize("&eSaving schematic..."));
                
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    boolean success = plugin.getArenaManager().saveSchematic(arena);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.sendMessage(HexUtils.colorize("&aSchematic saved successfully!"));
                        } else {
                            player.sendMessage(HexUtils.colorize("&cFailed to save schematic!"));
                        }
                    });
                });
            } else {
                player.sendMessage(HexUtils.colorize("&cArena is incomplete! Set all locations first."));
            }
        } else if (slot == guiConfig.getInt("items.back.slot", 49)) {
            plugin.getGuiManager().openArenaListGui(player);
        }
    }

    private void openAllowedKitsGui(Player player, Arena arena) {
        String title = HexUtils.colorize("&6Allowed Kits - " + arena.getName());
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        // Fill with glass panes
        ItemStack glassPane = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", new ArrayList<>());
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Add kits
        int slot = 10;
        for (Kit kit : plugin.getKitManager().getAllKits()) {
            if (slot >= 44) break; // Don't go past row 4
            
            boolean isAllowed = arena.isKitAllowed(kit.getName());
            ItemStack kitItem = createKitItem(kit, isAllowed);
            inventory.setItem(slot, kitItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip to next row
        }
        
        // Back button
        ItemStack backItem = createItem(Material.ARROW, HexUtils.colorize("&cBack"), 
            List.of(HexUtils.colorize("&7Return to arena editor")));
        inventory.setItem(49, backItem);
        
        player.openInventory(inventory);
    }

    private ItemStack createKitItem(Kit kit, boolean isAllowed) {
        Material material = isAllowed ? Material.LIME_DYE : Material.GRAY_DYE;
        String name = HexUtils.colorize("&f" + kit.getName());
        
        List<String> lore = new ArrayList<>();
        if (isAllowed) {
            lore.add(HexUtils.colorize("&aEnabled"));
            lore.add(HexUtils.colorize("&7Click to disable"));
        } else {
            lore.add(HexUtils.colorize("&cDisabled"));
            lore.add(HexUtils.colorize("&7Click to enable"));
        }
        
        return createItem(material, name, lore);
    }

    private String getArenaNameFromTitle(String title) {
        // Extract arena name from title if needed
        // For now, we'll need to store this differently or pass it as metadata
        return ""; // This needs to be implemented properly
    }
}
