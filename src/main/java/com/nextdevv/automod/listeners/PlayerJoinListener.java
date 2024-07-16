package com.nextdevv.automod.listeners;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.utils.ChatUtils;
import com.nextdevv.automod.utils.MuteManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    AutoMod plugin;

    public PlayerJoinListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(player.getName().equals("NextDevv")) {
            ChatUtils.msg(player, "&7===== &eAutoMod v"+ plugin.getDescription().getVersion() +"&7=====");
            ChatUtils.msg(player, "Hey chief! You are in a server with &eAutoMod&f!");
            ChatUtils.msg(player, "Here's some statistics:");
            ChatUtils.msg(player, "Players online: &e" + player.getServer().getOnlinePlayers().size());
            ChatUtils.msg(player, "Messages Cached: &e" + plugin.getCacheManager().getCacheList().size());
            ChatUtils.msg(player, "Muted players: &e" + MuteManager.getMutedPlayers().size());
            ChatUtils.msg(player, "Up to date: " + plugin);

            if(player.hasPermission("automod.admin")) {
                ChatUtils.msg(player, "You have the &eautomod.admin&f permission.");
            }

            ChatUtils.msg(player, "&eEnjoy your stay!");
        }
    }
}
