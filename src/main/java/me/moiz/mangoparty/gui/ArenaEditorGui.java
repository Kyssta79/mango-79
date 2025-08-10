package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArenaEditorGui implements Listener {
    private MangoParty plugin;

    public ArenaEditorGui(MangoParty plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openArenaListGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6Arena Manager");
        
        // Add create new arena button
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§a§lCreate New Arena");
        List<String> createLore = new ArrayList<>();
        createLore.add("§7Click to create a new arena");
        createMeta.setLore(createLore);
        createButton.setItemMeta(createMeta);
        gui.setItem(0, createButton);
        
        // Add existing arenas
        int slot = 9; // Start from second row
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (slot >= 54) break;
            
            ItemStack arenaItem = new ItemStack(arena.isComplete() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
            ItemMeta meta = arenaItem.getItemMeta();
            meta.setDisplayName("§e" + arena.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Status: " + (arena.isComplete() ? "§aComplete" : "§cIncomplete"));
            lore.add("§7Center: " + formatLocation(arena.getCenter()));
            lore.add("§7Spawn 1: " + formatLocation(arena.getSpawn1()));
            lore.add("§7Spawn 2: " + formatLocation(arena.getSpawn2()));
            lore.add("§7Corner 1: " + formatLocation(arena.getCorner1()));
            lore.add("§7Corner 2: " + formatLocation(arena.getCorner2()));
            lore.add("§7Allowed Kits: §f" + arena.getAllowedKits().size());
            lore.add("");
            lore.add("§aLeft-click to edit");
            lore.add("§cRight-click to delete");
            
            meta.setLore(lore);
            arenaItem.setItemMeta(meta);
            gui.setItem(slot, arenaItem);
            slot++;
        }
        
        player.openInventory(gui);
    }
    
    private String formatLocation(Location loc) {
        if (loc == null) return "§cNot set";
        return String.format("§f%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }

    public void openArenaEditorGui(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player.sendMessage("§cArena not found!");
            return;
        }
        
        ArenaEditorInstance instance = new ArenaEditorInstance(plugin, player, arena);
        instance.open();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals("§6Arena Manager")) {
            event.setCancelled(true);
            handleArenaListClick(player, event);
        }
    }
    
    private void handleArenaListClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.EMERALD) {
            // Create new arena
            player.closeInventory();
            player.sendMessage("§aType the name of the new arena in chat:");
            // TODO: Implement chat input system for arena name
            return;
        }
        
        if (clicked.getType() == Material.GREEN_CONCRETE || clicked.getType() == Material.RED_CONCRETE) {
            String arenaName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            
            if (event.isLeftClick()) {
                // Edit arena
                openArenaEditorGui(player, arenaName);
            } else if (event.isRightClick()) {
                // Delete arena
                plugin.getArenaManager().deleteArena(arenaName);
                player.sendMessage("§cDeleted arena: " + arenaName);
                openArenaListGui(player); // Refresh the GUI
            }
        }
    }

    public void reloadConfigs() {
        plugin.getLogger().info("Arena editor configs reloaded.");
    }

    // Inner class for individual arena editor instances
    private static class ArenaEditorInstance implements Listener {
        private MangoParty plugin;
        private Player player;
        private Arena arena;
        private Inventory inventory;
        private String currentLocationSetting;
        private BukkitTask timeoutTask;
        private boolean isSettingLocation = false;

        public ArenaEditorInstance(MangoParty plugin, Player player, Arena arena) {
            this.plugin = plugin;
            this.player = player;
            this.arena = arena;
            this.currentLocationSetting = null;
            this.isSettingLocation = false;
            
            createInventory();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        private void createInventory() {
            inventory = Bukkit.createInventory(null, 54, "Arena Editor: " + arena.getName());
            updateInventory();
        }

        private void updateInventory() {
            inventory.clear();
            
            // Arena status
            ItemStack statusItem = new ItemStack(arena.isComplete() ? Material.EMERALD : Material.REDSTONE);
            ItemMeta statusMeta = statusItem.getItemMeta();
            statusMeta.setDisplayName(ChatColor.GOLD + "Arena Status");
            List<String> statusLore = new ArrayList<>();
            statusLore.add(ChatColor.GRAY + "Complete: " + (arena.isComplete() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "Not set"));
            statusLore.add("");
            statusLore.add(ChatColor.YELLOW + "Required locations:");
            statusLore.add(ChatColor.GRAY + "• Center: " + (arena.getCenter() != null ? ChatColor.GREEN + "Set" : ChatColor.RED + "Not set"));
            statusLore.add(ChatColor.GRAY + "• Spawn 1: " + (arena.getSpawn1() != null ? ChatColor.GREEN + "Set" : ChatColor.RED + "Not set"));
            statusLore.add(ChatColor.GRAY + "• Spawn 2: " + (arena.getSpawn2() != null ? ChatColor.GREEN + "Set" : ChatColor.RED + "Not set"));
            statusLore.add(ChatColor.GRAY + "• Corner 1: " + (arena.getCorner1() != null ? ChatColor.GREEN + "Set" : ChatColor.RED + "Not set"));
            statusLore.add(ChatColor.GRAY + "• Corner 2: " + (arena.getCorner2() != null ? ChatColor.GREEN + "Set" : ChatColor.RED + "Not set"));
            statusMeta.setLore(statusLore);
            statusItem.setItemMeta(statusMeta);
            inventory.setItem(4, statusItem);

            // Location setting buttons
            inventory.setItem(10, createLocationButton("Center", Material.BEACON, arena.getCenter()));
            inventory.setItem(12, createLocationButton("Spawn 1", Material.RED_BED, arena.getSpawn1()));
            inventory.setItem(14, createLocationButton("Spawn 2", Material.BLUE_BED, arena.getSpawn2()));
            inventory.setItem(16, createLocationButton("Corner 1", Material.STONE, arena.getCorner1()));
            inventory.setItem(19, createLocationButton("Corner 2", Material.COBBLESTONE, arena.getCorner2()));

            // Kit management
            ItemStack kitItem = new ItemStack(Material.CHEST);
            ItemMeta kitMeta = kitItem.getItemMeta();
            kitMeta.setDisplayName(ChatColor.AQUA + "Manage Allowed Kits");
            List<String> kitLore = new ArrayList<>();
            kitLore.add(ChatColor.GRAY + "Click to manage which kits");
            kitLore.add(ChatColor.GRAY + "are allowed in this arena");
            kitLore.add("");
            kitLore.add(ChatColor.YELLOW + "Currently allowed: " + arena.getAllowedKits().size());
            kitMeta.setLore(kitLore);
            kitItem.setItemMeta(kitMeta);
            inventory.setItem(22, kitItem);

            // Save schematic button
            ItemStack saveItem = new ItemStack(Material.BOOK);
            ItemMeta saveMeta = saveItem.getItemMeta();
            saveMeta.setDisplayName(ChatColor.GREEN + "Save Schematic");
            List<String> saveLore = new ArrayList<>();
            if (arena.isComplete()) {
                saveLore.add(ChatColor.GRAY + "Click to save the arena");
                saveLore.add(ChatColor.GRAY + "blocks as a schematic");
                saveLore.add("");
                saveLore.add(ChatColor.GREEN + "✓ Arena is complete");
            } else {
                saveLore.add(ChatColor.RED + "Arena must be complete");
                saveLore.add(ChatColor.RED + "to save schematic");
            }
            saveMeta.setLore(saveLore);
            saveItem.setItemMeta(saveMeta);
            inventory.setItem(31, saveItem);

            // Clone arena button
            ItemStack cloneItem = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta cloneMeta = cloneItem.getItemMeta();
            cloneMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Clone Arena");
            List<String> cloneLore = new ArrayList<>();
            if (arena.isComplete()) {
                cloneLore.add(ChatColor.GRAY + "Click to clone this arena");
                cloneLore.add(ChatColor.GRAY + "to your current location");
                cloneLore.add("");
                cloneLore.add(ChatColor.GREEN + "✓ Arena is complete");
            } else {
                cloneLore.add(ChatColor.RED + "Arena must be complete");
                cloneLore.add(ChatColor.RED + "to clone");
            }
            cloneMeta.setLore(cloneLore);
            cloneItem.setItemMeta(cloneMeta);
            inventory.setItem(40, cloneItem);

            // Back button
            ItemStack backItem = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backItem.getItemMeta();
            backMeta.setDisplayName(ChatColor.RED + "Back to Arena List");
            backItem.setItemMeta(backMeta);
            inventory.setItem(45, backItem);
        }

        private ItemStack createLocationButton(String name, Material material, Location location) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Set " + name);
            
            List<String> lore = new ArrayList<>();
            if (location != null) {
                lore.add(ChatColor.GREEN + "✓ Currently set:");
                lore.add(ChatColor.GRAY + "World: " + location.getWorld().getName());
                lore.add(ChatColor.GRAY + "X: " + String.format("%.1f", location.getX()));
                lore.add(ChatColor.GRAY + "Y: " + String.format("%.1f", location.getY()));
                lore.add(ChatColor.GRAY + "Z: " + String.format("%.1f", location.getZ()));
            } else {
                lore.add(ChatColor.RED + "✗ Not set");
            }
            lore.add("");
            lore.add(ChatColor.AQUA + "Click to set location");
            lore.add(ChatColor.GRAY + "Then Shift+Left-Click in air");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        public void open() {
            player.openInventory(inventory);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getInventory().equals(inventory)) return;
            if (!(event.getWhoClicked() instanceof Player)) return;
            
            event.setCancelled(true);
            Player clicker = (Player) event.getWhoClicked();
            
            if (!clicker.equals(player)) return;
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            
            if (displayName.contains("Set Center")) {
                startLocationSetting("center");
            } else if (displayName.contains("Set Spawn 1")) {
                startLocationSetting("spawn1");
            } else if (displayName.contains("Set Spawn 2")) {
                startLocationSetting("spawn2");
            } else if (displayName.contains("Set Corner 1")) {
                startLocationSetting("corner1");
            } else if (displayName.contains("Set Corner 2")) {
                startLocationSetting("corner2");
            } else if (displayName.contains("Manage Allowed Kits")) {
                openKitManagementGui();
            } else if (displayName.contains("Save Schematic")) {
                if (arena.isComplete()) {
                    if (plugin.getArenaManager().saveSchematic(arena)) {
                        player.sendMessage(ChatColor.GREEN + "Schematic saved successfully!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to save schematic!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Arena must be complete to save schematic!");
                }
            } else if (displayName.contains("Clone Arena")) {
                if (arena.isComplete()) {
                    String cloneName = plugin.getArenaManager().cloneArena(arena, player.getLocation());
                    if (cloneName != null) {
                        player.sendMessage(ChatColor.GREEN + "Arena cloned successfully as: " + cloneName);
                    } else {
                        player.sendMessage(ChatColor.RED + "Failed to clone arena!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Arena must be complete to clone!");
                }
            } else if (displayName.contains("Back to Arena List")) {
                cleanup();
                plugin.getArenaEditorGui().openArenaListGui(player);
            }
        }

        private void startLocationSetting(String locationType) {
            currentLocationSetting = locationType;
            isSettingLocation = true;
            
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Setting " + locationType + " location...");
            player.sendMessage(ChatColor.AQUA + "Move to the desired location and Shift+Left-Click in air");
            player.sendMessage(ChatColor.GRAY + "You have 30 seconds to set the location");
            
            // Start timeout task
            timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (isSettingLocation) {
                    isSettingLocation = false;
                    currentLocationSetting = null;
                    player.sendMessage(ChatColor.RED + "Location setting timed out!");
                    
                    // Reopen GUI after timeout
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        updateInventory();
                        open();
                    }, 10L);
                }
            }, 600L); // 30 seconds
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player)) return;
            if (!isSettingLocation) return;
            if (currentLocationSetting == null) return;
            
            // Check for Shift+Left-Click in air
            if (event.getAction().name().contains("LEFT_CLICK") && 
                event.getPlayer().isSneaking() && 
                (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)) {
                
                event.setCancelled(true);
                
                Location location = player.getLocation();
                
                // Set the location based on type
                switch (currentLocationSetting.toLowerCase()) {
                    case "center":
                        arena.setCenter(location);
                        break;
                    case "spawn1":
                        arena.setSpawn1(location);
                        break;
                    case "spawn2":
                        arena.setSpawn2(location);
                        break;
                    case "corner1":
                        arena.setCorner1(location);
                        break;
                    case "corner2":
                        arena.setCorner2(location);
                        break;
                }
                
                // Save arena
                plugin.getArenaManager().saveArena(arena);
                
                // Cancel timeout task
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    timeoutTask = null;
                }
                
                // Reset state
                isSettingLocation = false;
                String locationType = currentLocationSetting;
                currentLocationSetting = null;
                
                player.sendMessage(ChatColor.GREEN + "Location set for " + locationType + "!");
                
                // Reopen GUI after short delay
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updateInventory();
                    open();
                }, 10L);
            }
        }

        private void openKitManagementGui() {
            Inventory kitGui = Bukkit.createInventory(null, 54, "Kit Management: " + arena.getName());
            
            Map<String, Kit> allKits = plugin.getKitManager().getKits();
            int slot = 0;
            
            for (Map.Entry<String, Kit> entry : allKits.entrySet()) {
                if (slot >= 45) break; // Leave space for control buttons
                
                String kitName = entry.getKey();
                Kit kit = entry.getValue();
                
                ItemStack kitItem = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.IRON_SWORD);
                ItemMeta meta = kitItem.getItemMeta();
                
                // Fixed logic: empty list = all disabled, non-empty list = only those enabled
                boolean isAllowed = arena.getAllowedKits().contains(kitName);
                
                meta.setDisplayName(ChatColor.YELLOW + kitName);
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GRAY + "Status: " + (isAllowed ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
                lore.add("");
                lore.add(ChatColor.AQUA + "Click to toggle");
                meta.setLore(lore);
                
                kitItem.setItemMeta(meta);
                kitGui.setItem(slot, kitItem);
                slot++;
            }
            
            // Enable all button
            ItemStack enableAllItem = new ItemStack(Material.LIME_DYE);
            ItemMeta enableAllMeta = enableAllItem.getItemMeta();
            enableAllMeta.setDisplayName(ChatColor.GREEN + "Enable All Kits");
            enableAllItem.setItemMeta(enableAllMeta);
            kitGui.setItem(45, enableAllItem);
            
            // Disable all button
            ItemStack disableAllItem = new ItemStack(Material.RED_DYE);
            ItemMeta disableAllMeta = disableAllItem.getItemMeta();
            disableAllMeta.setDisplayName(ChatColor.RED + "Disable All Kits");
            disableAllItem.setItemMeta(disableAllMeta);
            kitGui.setItem(46, disableAllItem);
            
            // Back button
            ItemStack backItem = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backItem.getItemMeta();
            backMeta.setDisplayName(ChatColor.YELLOW + "Back to Arena Editor");
            backItem.setItemMeta(backMeta);
            kitGui.setItem(53, backItem);
            
            // Register temporary listener for kit GUI
            Listener kitListener = new Listener() {
                @EventHandler
                public void onKitClick(InventoryClickEvent e) {
                    if (!e.getInventory().equals(kitGui)) return;
                    if (!(e.getWhoClicked() instanceof Player)) return;
                    
                    e.setCancelled(true);
                    Player clicker = (Player) e.getWhoClicked();
                    
                    if (!clicker.equals(player)) return;
                    
                    ItemStack clicked = e.getCurrentItem();
                    if (clicked == null || clicked.getType() == Material.AIR) return;
                    
                    String displayName = clicked.getItemMeta().getDisplayName();
                    
                    if (displayName.contains("Enable All Kits")) {
                        // Enable all kits by adding all kit names to the list
                        List<String> allKitNames = new ArrayList<>(allKits.keySet());
                        arena.setAllowedKits(allKitNames);
                        plugin.getArenaManager().saveArena(arena);
                        player.sendMessage(ChatColor.GREEN + "Enabled all kits for this arena!");
                        openKitManagementGui(); // Refresh
                    } else if (displayName.contains("Disable All Kits")) {
                        // Disable all kits by clearing the list
                        arena.setAllowedKits(new ArrayList<>());
                        plugin.getArenaManager().saveArena(arena);
                        player.sendMessage(ChatColor.RED + "Disabled all kits for this arena!");
                        openKitManagementGui(); // Refresh
                    } else if (displayName.contains("Back to Arena Editor")) {
                        HandlerList.unregisterAll(this);
                        updateInventory();
                        open();
                    } else {
                        // Toggle individual kit
                        String kitName = ChatColor.stripColor(displayName);
                        List<String> allowedKits = new ArrayList<>(arena.getAllowedKits());
                        
                        if (allowedKits.contains(kitName)) {
                            // Kit is currently enabled, disable it
                            allowedKits.remove(kitName);
                            player.sendMessage(ChatColor.RED + "Disabled kit: " + kitName);
                        } else {
                            // Kit is currently disabled, enable it
                            allowedKits.add(kitName);
                            player.sendMessage(ChatColor.GREEN + "Enabled kit: " + kitName);
                        }
                        
                        arena.setAllowedKits(allowedKits);
                        plugin.getArenaManager().saveArena(arena);
                        openKitManagementGui(); // Refresh
                    }
                }
            };
            
            plugin.getServer().getPluginManager().registerEvents(kitListener, plugin);
            
            // Auto-unregister after 5 minutes
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                HandlerList.unregisterAll(kitListener);
            }, 6000L);
            
            player.openInventory(kitGui);
        }

        public void cleanup() {
            if (timeoutTask != null) {
                timeoutTask.cancel();
                timeoutTask = null;
            }
            isSettingLocation = false;
            currentLocationSetting = null;
            HandlerList.unregisterAll(this);
        }
    }
}
