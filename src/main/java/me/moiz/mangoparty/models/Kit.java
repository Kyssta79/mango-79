package me.moiz.mangoparty.models;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class Kit {
    private String name;
    private String displayName;
    private List<ItemStack> items;
    private List<ItemStack> armor;
    private ItemStack icon;
    private KitRules rules;
    
    public Kit(String name) {
        this.name = name;
        this.displayName = name;
        this.items = new ArrayList<>();
        this.armor = new ArrayList<>();
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
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    public void setItems(List<ItemStack> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
    
    public ItemStack[] getContents() {
        return items.toArray(new ItemStack[0]);
    }
    
    public void setContents(ItemStack[] contents) {
        this.items = new ArrayList<>();
        if (contents != null) {
            for (ItemStack item : contents) {
                if (item != null) {
                    this.items.add(item);
                }
            }
        }
    }
    
    public List<ItemStack> getArmor() {
        return armor;
    }
    
    public void setArmor(List<ItemStack> armor) {
        this.armor = armor != null ? armor : new ArrayList<>();
    }
    
    public ItemStack[] getArmorContents() {
        return armor.toArray(new ItemStack[0]);
    }
    
    public void setArmorContents(ItemStack[] armor) {
        this.armor = new ArrayList<>();
        if (armor != null) {
            for (ItemStack armorPiece : armor) {
                if (armorPiece != null) {
                    this.armor.add(armorPiece);
                }
            }
        }
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
        this.rules = rules != null ? rules : new KitRules();
    }
}
