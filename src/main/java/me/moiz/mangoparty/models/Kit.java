package me.moiz.mangoparty.models;

import org.bukkit.inventory.ItemStack;

public class Kit {
    private String name;
    private String displayName;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private ItemStack icon;
    private KitRules rules;
    
    public Kit(String name) {
        this.name = name;
        this.displayName = name;
        this.rules = new KitRules();
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public ItemStack[] getContents() {
        return contents;
    }
    
    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }
    
    public ItemStack[] getArmor() {
        return armor;
    }
    
    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }
    
    public ItemStack getIcon() {
        return icon;
    }
    
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }
    
    public KitRules getRules() {
        return rules;
    }
    
    public void setRules(KitRules rules) {
        this.rules = rules;
    }
}
