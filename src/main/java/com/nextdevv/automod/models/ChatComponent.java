package com.nextdevv.automod.models;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ChatComponent {
    String message;

    public ChatComponent(String message) {
        this.message = message;
    }

    public BaseComponent get() {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public String toString() {
        return message;
    }
}
