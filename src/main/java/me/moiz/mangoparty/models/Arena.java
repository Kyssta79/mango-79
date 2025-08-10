package me.moiz.mangoparty.models;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private String name;
    private Location center;
    private Location spawn1;
    private Location spawn2;
    private Location corner1;
    private Location corner2;
    private boolean regenerateBlocks;
    private List<String> allowedKits;

    public Arena(String name) {
        this.name = name;
        this.regenerateBlocks = true;
        this.allowedKits = new ArrayList<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    public Location getCorner1() {
        return corner1;
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
    }

    public boolean isRegenerateBlocks() {
        return regenerateBlocks;
    }

    public void setRegenerateBlocks(boolean regenerateBlocks) {
        this.regenerateBlocks = regenerateBlocks;
    }

    public List<String> getAllowedKits() {
        return allowedKits;
    }

    public void setAllowedKits(List<String> allowedKits) {
        this.allowedKits = allowedKits != null ? allowedKits : new ArrayList<>();
    }

    public void addAllowedKit(String kitName) {
        if (!allowedKits.contains(kitName)) {
            allowedKits.add(kitName);
        }
    }

    public void removeAllowedKit(String kitName) {
        allowedKits.remove(kitName);
    }

    public boolean isKitAllowed(String kitName) {
        return allowedKits.isEmpty() || allowedKits.contains(kitName);
    }

    public boolean isComplete() {
        return center != null && spawn1 != null && spawn2 != null && corner1 != null && corner2 != null;
    }

    public void saveToConfig(ConfigurationSection section) {
        if (center != null) {
            ConfigurationSection centerSection = section.createSection("center");
            saveLocationToConfig(centerSection, center);
        }
        if (spawn1 != null) {
            ConfigurationSection spawn1Section = section.createSection("spawn1");
            saveLocationToConfig(spawn1Section, spawn1);
        }
        if (spawn2 != null) {
            ConfigurationSection spawn2Section = section.createSection("spawn2");
            saveLocationToConfig(spawn2Section, spawn2);
        }
        if (corner1 != null) {
            ConfigurationSection corner1Section = section.createSection("corner1");
            saveLocationToConfig(corner1Section, corner1);
        }
        if (corner2 != null) {
            ConfigurationSection corner2Section = section.createSection("corner2");
            saveLocationToConfig(corner2Section, corner2);
        }
        section.set("regenerateBlocks", regenerateBlocks);
        section.set("allowedKits", allowedKits);
    }

    private void saveLocationToConfig(ConfigurationSection section, Location location) {
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }
}
