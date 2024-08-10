package com.nextdevv.automod.listeners;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.api.PerspectiveAPI;
import com.nextdevv.automod.manager.MuteManager;
import com.nextdevv.automod.models.Cache;
import com.nextdevv.automod.utils.LinkDetector;
import com.nextdevv.automod.utils.ListUtils;
import com.nextdevv.automod.utils.Pair;
import com.nextdevv.automod.utils.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static com.nextdevv.automod.utils.ChatUtils.msg;
import static com.nextdevv.automod.utils.ChatUtils.msgButton;


public class AsyncSignChangeListener implements Listener {
    private final AutoMod plugin;

    public AsyncSignChangeListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines().clone();

        if(player.hasPermission("automod.bypass") || player.isOp()) return;
        if(!plugin.getSettings().isSignModeration()) return;

        PerspectiveAPI api = plugin.getPerspectiveAPI();
        if (api == null)
            return;

        boolean playerMuted = MuteManager.isMuted(player.getUniqueId());
        if(!plugin.getSettings().isMutedCanWriteSigns()) {
            if(playerMuted) {
                msg(player, plugin.getMessages().getCannotPlaceSign());
                event.setCancelled(true);
                return;
            }
        }

        for(int i = 0; i < 4; i++)
            event.setLine(i, "");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(plugin.getSettings().isDebug()) {
                        plugin.getLogger().info("[DEBUG] Censoring sign text: " + ListUtils.toString(lines, ", "));
                    }

                    if(plugin.getSettings().isSignCaching()) {
                        Cache cache = plugin.getCacheManager().getCache(ListUtils.toString(lines, " "));
                        if(cache != null) {
                            String[] censored = StringUtils.splitOrEmpty(cache.censored(), ", ");
                            updateSign(event, censored, lines, cache.toxic());
                            return;
                        }
                    }

                    Pair<String, Boolean> pair = api.censorAsync(ListUtils.toString(lines, ", ")).get();

                    String[] censored = StringUtils.splitOrEmpty(pair.getFirst(), ", ");
                    String[] blockedWords = plugin.getSettings().getBlockedWords();

                    for (String line : censored) {
                        String censor = LinkDetector.censor(line, plugin.getSettings().getCensorCharacters());
                        ListUtils.replace(censored, line, censor);
                    }

                    for(String word : blockedWords) {
                        for (String line : censored) {
                            String censor = line;
                            if (line.contains(word)) {
                                censor = StringUtils.censor(censor, word, plugin.getSettings().getCensorCharacters());
                            }

                            ListUtils.replace(censored, line, censor);
                        }
                    }

                    updateSign(event, censored, lines, pair.getSecond());

                    Cache newCache = new Cache(ListUtils.toString(lines, " "), ListUtils.toString(censored, " "), pair.getSecond());
                    plugin.getCacheManager().addCache(newCache);
                }catch (Exception e) {
                    plugin.getLogger().severe("Error processing sign: " + e.getMessage());
                    if(plugin.getSettings().isDebug()) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void updateSign(SignChangeEvent event, String[] censored, String[] original, boolean toxic) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Block block = event.getBlock();
                Sign sign = (Sign) block.getState();
                String message = ListUtils.toString(original, " ").trim();

                if(plugin.getSettings().isDebug()) {
                    plugin.getLogger().info("[DEBUG] Updating sign: " + ListUtils.toString(censored, ", "));
                }

                for(int i = 0; i < 4; i++) {
                    if (i >= censored.length) break;
                    if (plugin.getSettings().isDebug()) {
                        plugin.getLogger().info("[DEBUG] Setting line " + i + " to " + censored[i] + " on sign " + sign.getLocation());
                    }
                    sign.setLine(i, censored[i]);
                }

                sign.update();

                if(plugin.getSettings().isNotifyStaffSigns()) {
                    String toxicMessage = toxic ? " &c[TOXIC]&r" : "";
                    for(Player staff : plugin.getServer().getOnlinePlayers()) {
                        if(staff.hasPermission("automod.staff")) {
                            msgButton(staff, plugin.getSettings().getNotifyStaffSignsFormat()
                                            .replace("{prefix}", plugin.getMessages().getPrefix())
                                            .replace("{player}", event.getPlayer().getName())
                                            .replace("{message}", message)
                                            .replace("{toxic}", toxicMessage)
                                            .replace("{world}", block.getWorld().getName())
                                            .replace("{x}", String.valueOf(block.getX()))
                                            .replace("{y}", String.valueOf(block.getY()))
                                            .replace("{z}", String.valueOf(block.getZ())),
                                    "&eTeleport", plugin.getSettings().getTpCommand()
                                            .replace("{player}", staff.getName())
                                            .replace("{target}", event.getPlayer().getName())
                                            .replace("{world}", block.getWorld().getName())
                                            .replace("{x}", String.valueOf(block.getX()))
                                            .replace("{y}", String.valueOf(block.getY()))
                                            .replace("{z}", String.valueOf(block.getZ()))
                            );
                        }
                    }
                }

                if(plugin.getSettings().isLogSigns())
                    plugin.getChatLogger().logSign(event.getPlayer(), block.getLocation(), ListUtils.toString(original, " "), toxic);
            }
        }.runTask(plugin);
    }
}
