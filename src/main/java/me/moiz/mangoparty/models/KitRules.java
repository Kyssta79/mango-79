package me.moiz.mangoparty.models;

public class KitRules {
    private boolean naturalHealthRegen;
    private boolean blockBreak;
    private boolean blockPlace;
    private double damageMultiplier;
    private boolean instantTnt;
    
    public KitRules() {
        // Default values
        this.naturalHealthRegen = true;
        this.blockBreak = false;
        this.blockPlace = false;
        this.damageMultiplier = 1.0;
        this.instantTnt = false;
    }
    
    // Natural Health Regeneration
    public boolean isNaturalHealthRegen() {
        return naturalHealthRegen;
    }
    
    public boolean isNaturalHealthRegeneration() {
        return naturalHealthRegen;
    }
    
    public void setNaturalHealthRegen(boolean naturalHealthRegen) {
        this.naturalHealthRegen = naturalHealthRegen;
    }
    
    public void setNaturalHealthRegeneration(boolean naturalHealthRegen) {
        this.naturalHealthRegen = naturalHealthRegen;
    }
    
    // Block Breaking
    public boolean isBlockBreak() {
        return blockBreak;
    }
    
    public boolean isBlockBreaking() {
        return blockBreak;
    }
    
    public void setBlockBreak(boolean blockBreak) {
        this.blockBreak = blockBreak;
    }
    
    public void setBlockBreaking(boolean blockBreak) {
        this.blockBreak = blockBreak;
    }
    
    // Block Placing
    public boolean isBlockPlace() {
        return blockPlace;
    }
    
    public boolean isBlockPlacing() {
        return blockPlace;
    }
    
    public void setBlockPlace(boolean blockPlace) {
        this.blockPlace = blockPlace;
    }
    
    public void setBlockPlacing(boolean blockPlace) {
        this.blockPlace = blockPlace;
    }
    
    // Damage Multiplier
    public double getDamageMultiplier() {
        return damageMultiplier;
    }
    
    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }
    
    // Instant TNT
    public boolean isInstantTnt() {
        return instantTnt;
    }
    
    public void setInstantTnt(boolean instantTnt) {
        this.instantTnt = instantTnt;
    }
}
