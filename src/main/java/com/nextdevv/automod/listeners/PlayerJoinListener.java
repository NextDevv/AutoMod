package com.nextdevv.automod.listeners;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.api.PerspectiveAPI;
import com.nextdevv.automod.enums.Attribute;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PlayerJoinListener implements Listener {
    AutoMod plugin;

    public PlayerJoinListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equals("NextDevv")) {
            ChatUtils.msg(player, "&7===== &eAutoMod v" + plugin.getDescription().getVersion() + "&7=====");
            ChatUtils.msg(player, "Here's some statistics:");
            ChatUtils.msg(player, "Players online: &e" + player.getServer().getOnlinePlayers().size());
            ChatUtils.msg(player, "Messages Cached: &e" + plugin.getCacheManager().getCacheList().size());
            ChatUtils.msg(player, "Muted players: &e" + MuteManager.getMutedPlayers().size());
            ChatUtils.msg(player, "Up to date: " + plugin);

            if (player.hasPermission("automod.admin")) {
                ChatUtils.msg(player, "You have the &eautomod.admin&f permission.");
            }

            ChatUtils.msg(player, "&eEnjoy your stay!");
        }

        if (plugin.getSettings().isUsernameModeration()) {
            String username = player.getName();
            if (username.length() >= plugin.getSettings().getMaxUsernameLength()) {
                kick(player, plugin.getMessages().getUsernameTooLong());
                return;
            }

            if (Arrays.stream(plugin.getSettings().getBlockedUsernames()).anyMatch(s -> s.contains(username))) {
                kick(player, plugin.getMessages().getBlacklistedUsername());
                return;
            }

            if (plugin.getSettings().isAllowAiUsernameModeration()) {
                try {
                    plugin.getPerspectiveAPI().censorAsync(username, Attribute.PROFANITY);
                } catch (URISyntaxException e) {
                    plugin.getLogger().severe("Unable to moderate the username: " + username);
                }
            }
        }

        try {
            if(Arrays.stream(plugin.getSettings().getExcludeUsers()).toList().contains(player.getName())) return;
            boolean isVpnOrProxy = plugin.getVpnProxyDetector().isVpnProxy(Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress()).get();
            if (isVpnOrProxy && plugin.getSettings().isVpnKick()) {
                kick(player, plugin.getMessages().getVpnProxy());
            }
        } catch (InterruptedException | ExecutionException e) {
            plugin.getLogger().severe("Failed to check if " + player.getName() + " is using a VPN or Proxy.");
            if(plugin.getSettings().isDebug())
                throw new RuntimeException(e);
        }
    }

    private void kick(Player player, String message) {
        player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                message.replace("{prefix}", plugin.getMessages().getPrefix())));
    }
}