package me.moiz.mangoparty.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final boolean SUPPORTS_HEX = isVersionSupported();
    
    private static boolean isVersionSupported() {
        try {
            // Check if the server version supports hex colors (1.16+)
            String version = Bukkit.getVersion();
            String[] versionParts = version.split("\\.");
            if (versionParts.length >= 2) {
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1].split("-")[0]);
                return major > 1 || (major == 1 && minor >= 16);
            }
        } catch (Exception e) {
            // Fallback to checking if ChatColor.of method exists
            try {
                ChatColor.class.getMethod("of", String.class);
                return true;
            } catch (NoSuchMethodException ex) {
                return false;
            }
        }
        return false;
    }
    
    public static String colorize(String message) {
        if (message == null) return null;
        
        if (SUPPORTS_HEX) {
            // Process hex colors
            Matcher matcher = HEX_PATTERN.matcher(message);
            while (matcher.find()) {
                String hexCode = matcher.group(1);
                String replacement = ChatColor.of("#" + hexCode).toString();
                message = message.replace("&#" + hexCode, replacement);
            }
        } else {
            // Fallback to legacy colors for older versions
            message = convertHexToLegacy(message);
        }
        
        // Process legacy color codes
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    public static String colorify(String message) {
        return colorize(message);
    }
    
    private static String convertHexToLegacy(String message) {
        // Convert common hex colors to legacy equivalents
        message = message.replaceAll("&#FF0000", "&c"); // Red
        message = message.replaceAll("&#00FF00", "&a"); // Green
        message = message.replaceAll("&#0000FF", "&9"); // Blue
        message = message.replaceAll("&#FFFF00", "&e"); // Yellow
        message = message.replaceAll("&#FF00FF", "&d"); // Magenta
        message = message.replaceAll("&#00FFFF", "&b"); // Cyan
        message = message.replaceAll("&#FFFFFF", "&f"); // White
        message = message.replaceAll("&#000000", "&0"); // Black
        message = message.replaceAll("&#808080", "&8"); // Gray
        message = message.replaceAll("&#C0C0C0", "&7"); // Light Gray
        message = message.replaceAll("&#800000", "&4"); // Dark Red
        message = message.replaceAll("&#008000", "&2"); // Dark Green
        message = message.replaceAll("&#000080", "&1"); // Dark Blue
        message = message.replaceAll("&#808000", "&6"); // Gold
        message = message.replaceAll("&#800080", "&5"); // Dark Purple
        message = message.replaceAll("&#008080", "&3"); // Dark Aqua
        
        // Remove any remaining hex patterns
        message = HEX_PATTERN.matcher(message).replaceAll("&f");
        
        return message;
    }
    
    public static String stripColor(String message) {
        if (message == null) return null;
        
        // Remove hex colors
        message = HEX_PATTERN.matcher(message).replaceAll("");
        
        // Remove legacy colors
        message = ChatColor.stripColor(message);
        
        return message;
    }
    
    public static boolean supportsHex() {
        return SUPPORTS_HEX;
    }
}
