package com.nextdevv.automod.utils;

import com.nextdevv.automod.AutoMod;
import org.bukkit.plugin.java.JavaPlugin;

public class Debug {
    private static final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    public static void log(String message) {
        if(plugin.getSettings().isDebug())
            plugin.getLogger().info("[DEBUG] "+message);
    }

    public static void verbose(String message) {
        if(plugin.getSettings().isVerbose() && plugin.getSettings().isDebug())
            plugin.getLogger().info("[VERBOSE] "+message);
    }
}
