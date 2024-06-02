package it.unilix.automod.utils;

import com.google.gson.JsonObject;
import it.unilix.automod.AutoMod;
import it.unilix.automod.enums.ModEvent;
import kotlin.Pair;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager {
    @Getter
    private static final HashMap<UUID, Long> mutedPlayers = new HashMap<>();
    private static final HashMap<UUID, Pair<Long, Integer>> warnings = new HashMap<>();
    private static final AutoMod plugin = JavaPlugin.getPlugin(AutoMod.class);

    public static void mutePlayer(UUID player) {
        mutedPlayers.put(player, System.currentTimeMillis());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.MUTE.getName());
        jsonObject.addProperty("player", player.toString());

        plugin.getRedisManager().publish(jsonObject);
    }

    public static void unmutePlayer(UUID player) {
        mutedPlayers.remove(player);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.UNMUTE.getName());
        jsonObject.addProperty("player", player.toString());

        plugin.getRedisManager().publish(jsonObject);
    }

    public static boolean isMuted(UUID player) {
        return mutedPlayers.containsKey(player);
    }

    public static long getMuteTime(UUID player) {
        return mutedPlayers.get(player) == null ? System.currentTimeMillis() : mutedPlayers.get(player);
    }

    public static void warnPlayer(UUID player) {
        if (warnings.containsKey(player)) {
            warnings.put(player, new Pair<>(warnings.get(player).getFirst(), warnings.get(player).getSecond() + 1));
        } else {
            warnings.put(player, new Pair<>(System.currentTimeMillis(), 1));
        }
    }

    public static int getWarnings(UUID player) {
        if (warnings.get(player) == null) {
            return 0;
        }

        return warnings.get(player).getSecond();
    }

    public static void clearWarnings(UUID player) {
        warnings.remove(player);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("event", ModEvent.CLEAR_WARNINGS.getName());
        jsonObject.addProperty("player", player.toString());

        plugin.getRedisManager().publish(jsonObject);
    }

    /*public static void updater(AutoMod plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<UUID> toRemove = new ArrayList<>();
                mutedPlayers.forEach((uuid, time) -> {
                    if (System.currentTimeMillis() - time >= plugin.getSettings().getMuteTime()) {
                        toRemove.add(uuid);
                    }
                });

                toRemove.forEach(uuid -> {
                    mutedPlayers.remove(uuid);
                    clearWarnings(uuid);
                    Objects.requireNonNull(Bukkit.getPlayer(uuid))
                            .sendMessage(
                                    ChatColor.translateAlternateColorCodes('&',
                                            plugin.getMessages().getUnmuted()
                                    )
                            );
                });

                warnings.forEach((uuid, warn) -> {
                    if (System.currentTimeMillis() - warn.getFirst() >= plugin.getSettings().getWarnExpireTime()) {
                        warnings.remove(uuid);
                    }
                });
            }
        }.runTaskAsynchronously(plugin);
    }*/

    public static Long getWarnTime(UUID player) {
        return warnings.get(player) == null ? System.currentTimeMillis() : warnings.get(player).getFirst();
    }

    public static void checkPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if(System.currentTimeMillis() - getMuteTime(uuid) >= plugin.getSettings().getMuteTime()) {
            unmutePlayer(uuid);
            clearWarnings(uuid);
        }

        if(System.currentTimeMillis() - getWarnTime(uuid) >= plugin.getSettings().getWarnExpireTime()) {
            clearWarnings(uuid);
        }
    }
}
