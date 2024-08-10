package com.nextdevv.automod.api;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.manager.MuteManager;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class LiteBans {
    private final AutoMod plugin;

    public LiteBans(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                switch (entry.getType()) {
                    case "warn" -> {
                        if(entry.getUuid() == null) return;
                        MuteManager.warnPlayer(UUID.fromString(entry.getUuid()));
                    }

                    case "mute" -> {
                        if(entry.getUuid() == null) return;
                        MuteManager.mutePlayer(UUID.fromString(entry.getUuid()));
                    }
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
                switch (entry.getType()) {
                    case "warn" -> {
                        if(entry.getUuid() == null) return;
                        MuteManager.clearWarnings(UUID.fromString(entry.getUuid()));
                    }

                    case "mute" -> {
                        if(entry.getUuid() == null) return;
                        MuteManager.unmutePlayer(UUID.fromString(entry.getUuid()));
                    }
                }
            }
        });
    }

    public void mutePlayer(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                assert player != null;
                String muteCommand = plugin.getSettings().getMuteCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), muteCommand);
            }
        }.runTask(plugin);
    }

    public void unmutePlayer(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                assert player != null;
                String unmuteCommand = plugin.getSettings().getUnmuteCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unmuteCommand);
            }
        }.runTask(plugin);
    }

    public void warnPlayer(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                assert player != null;
                String warnCommand = plugin.getSettings().getWarnCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), warnCommand);
            }
        }.runTask(plugin);
    }

    public void clearWarnings(UUID uuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(uuid);
                assert player != null;
                String clearWarningsCommand = plugin.getSettings().getUnwarnCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearWarningsCommand);
            }
        }.runTask(plugin);
    }

    public boolean isMuted(UUID uuid) {
        return Database.get().isPlayerMuted(uuid, null);
    }
}
