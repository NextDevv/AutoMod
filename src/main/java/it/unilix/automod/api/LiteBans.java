package it.unilix.automod.api;

import it.unilix.automod.AutoMod;
import it.unilix.automod.utils.MuteManager;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        String muteCommand = plugin.getSettings().getMuteCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), muteCommand);
    }

    public void unmutePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        String unmuteCommand = plugin.getSettings().getUnmuteCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), unmuteCommand);
    }

    public void warnPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        String warnCommand = plugin.getSettings().getWarnCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), warnCommand);
    }

    public void clearWarnings(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        String clearWarningsCommand = plugin.getSettings().getUnwarnCommand().replace("{player}", player.getName()) + "--sender=AutoMod";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearWarningsCommand);
    }
}
