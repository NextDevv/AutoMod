package com.nextdevv.automod.utils;

import com.nextdevv.automod.AutoMod;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChatUtils {
    public static void msg(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                .replace("{prefix}", AutoMod.getPlugin(AutoMod.class).getMessages().getPrefix())));
    }

    public static void msgButton(CommandSender sender, String message, String button, String command) {
        TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.translateAlternateColorCodes('&', button))}));
        sender.spigot().sendMessage(textComponent);
    }
}
