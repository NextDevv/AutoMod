package com.nextdevv.automod.models;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatButton extends ChatComponent {
    String command;
    String hover;

    public ChatButton(String message, String command, String hover) {
        super(message);

        this.command = command;
        this.hover = hover;
    }

    @Override
    public TextComponent get() {
        TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(org.bukkit.ChatColor.translateAlternateColorCodes('&', hover))}));
        return textComponent;
    }
}
