package me.moiz.mangoparty;

import me.moiz.mangoparty.commands.*;
import me.moiz.mangoparty.config.ConfigManager;
import me.moiz.mangoparty.gui.GuiManager;
import me.moiz.mangoparty.gui.ArenaEditorGui;
import me.moiz.mangoparty.gui.KitEditorGui;
import me.moiz.mangoparty.listeners.*;
import me.moiz.mangoparty.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class MangoParty extends JavaPlugin {
    
    private PartyManager partyManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private MatchManager matchManager;
    private ScoreboardManager scoreboardManager;
    private GuiManager guiManager;
    private ConfigManager configManager;
    private PartyDuelManager partyDuelManager;
    private QueueManager queueManager;
    private ArenaEditorGui arenaEditorGui;
    private KitEditorGui kitEditorGui;
    
    // Listeners
    private PlayerConnectionListener playerConnectionListener;
    private PlayerDeathListener playerDeathListener;
    private PlayerRespawnListener playerRespawnListener;
    private SpectatorListener spectatorListener;
    private KitRulesListener kitRulesListener;
    private ArenaBoundsListener arenaBoundsListener;
    
    private Location spawnLocation;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        partyManager = new PartyManager();
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        matchManager = new MatchManager(this);
        scoreboardManager = new ScoreboardManager(this);
        guiManager = new GuiManager(this);
        partyDuelManager = new PartyDuelManager(this);
        queueManager = new QueueManager(this);
        arenaEditorGui = new ArenaEditorGui(this);
        kitEditorGui = new KitEditorGui(this);
        
        // Initialize listeners
        playerConnectionListener = new PlayerConnectionListener(this);
        playerDeathListener = new PlayerDeathListener(this);
        playerRespawnListener = new PlayerRespawnListener(this);
        spectatorListener = new SpectatorListener(this);
        kitRulesListener = new KitRulesListener(this);
        arenaBoundsListener = new ArenaBoundsListener(this);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(playerConnectionListener, this);
        Bukkit.getPluginManager().registerEvents(playerDeathListener, this);
        Bukkit.getPluginManager().registerEvents(playerRespawnListener, this);
        Bukkit.getPluginManager().registerEvents(spectatorListener, this);
        Bukkit.getPluginManager().registerEvents(kitRulesListener, this);
        Bukkit.getPluginManager().registerEvents(arenaBoundsListener, this);
        
        // Register commands
        registerCommands();
        
        // Load spawn location
        loadSpawnLocation();
        
        // Startup message without color codes for console
        getLogger().info("MangoParty has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors().get(0));
    }
    
    @Override
    public void onDisable() {
        // Cleanup
        if (matchManager != null) {
            matchManager.cleanup();
        }
        if (queueManager != null) {
            queueManager.cleanup();
        }
        if (partyDuelManager != null) {
            partyDuelManager.cleanup();
        }
        
        getLogger().info("MangoParty has been disabled!");
    }
    
    private void registerCommands() {
        // Party commands
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("party").setTabCompleter(new PartyTabCompleter(this));
        
        // Spectate commands
        getCommand("spectate").setExecutor(new SpectateCommand(this));
        getCommand("spectate").setTabCompleter(new SpectateTabCompleter(this));
        
        // Admin commands
        getCommand("mango").setExecutor(new MangoCommand(this));
        getCommand("mango").setTabCompleter(new MangoTabCompleter(this));
        
        // Queue commands
        getCommand("1v1queue").setExecutor(new QueueCommand(this, "1v1"));
        getCommand("2v2queue").setExecutor(new QueueCommand(this, "2v2"));
        getCommand("3v3queue").setExecutor(new QueueCommand(this, "3v3"));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(this));
        
        // Tab completers for queue commands
        getCommand("1v1queue").setTabCompleter(new QueueTabCompleter());
        getCommand("2v2queue").setTabCompleter(new QueueTabCompleter());
        getCommand("3v3queue").setTabCompleter(new QueueTabCompleter());
        getCommand("leavequeue").setTabCompleter(new QueueTabCompleter());
    }
    
    private void loadSpawnLocation() {
        if (getConfig().contains("spawn")) {
            String worldName = getConfig().getString("spawn.world");
            double x = getConfig().getDouble("spawn.x");
            double y = getConfig().getDouble("spawn.y");
            double z = getConfig().getDouble("spawn.z");
            float yaw = (float) getConfig().getDouble("spawn.yaw");
            float pitch = (float) getConfig().getDouble("spawn.pitch");
            
            if (Bukkit.getWorld(worldName) != null) {
                spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
            }
        }
    }
    
    public void saveSpawnLocation(Location location) {
        getConfig().set("spawn.world", location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
        spawnLocation = location;
    }
    
    public void setSpawnLocation(Location location) {
        saveSpawnLocation(location);
    }
    
    // Getters
    public PartyManager getPartyManager() { return partyManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public KitManager getKitManager() { return kitManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public PartyDuelManager getPartyDuelManager() { return partyDuelManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public ArenaEditorGui getArenaEditorGui() { return arenaEditorGui; }
    public KitEditorGui getKitEditorGui() { return kitEditorGui; }
    public SpectatorListener getSpectatorListener() { return spectatorListener; }
    public Location getSpawnLocation() { return spawnLocation; }
}
