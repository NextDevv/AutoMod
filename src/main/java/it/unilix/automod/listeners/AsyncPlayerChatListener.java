package it.unilix.automod.listeners;

import it.unilix.automod.AutoMod;
import it.unilix.automod.enums.ModEvent;
import it.unilix.automod.models.Cache;
import it.unilix.automod.models.ChatEvent;
import it.unilix.automod.utils.LinkDetector;
import it.unilix.automod.utils.MuteManager;
import kotlin.Pair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class AsyncPlayerChatListener implements Listener {
    private final AutoMod plugin;

    public AsyncPlayerChatListener(AutoMod plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("automod.bypass") || player.isOp())
            return;
        MuteManager.checkPlayer(player);

        event.setCancelled(true);
        if(MuteManager.isMuted(event.getPlayer().getUniqueId())) {
            msg(player,plugin.getMessages().getMuted());
            return;
        }

        if(!LinkDetector.detect(event.getMessage()).isEmpty()) {
            MuteManager.warnPlayer(player.getUniqueId());
            if(MuteManager.getWarnings(player.getUniqueId()) >= 2) {
                MuteManager.mutePlayer(player.getUniqueId());
                MuteManager.clearWarnings(player.getUniqueId());
                msg(player, plugin.getMessages().getMuted());
                return;
            }

            msg(player, plugin.getMessages().getWarned());
            return;
        }

        new BukkitRunnable() {
            final String message = event.getMessage();
            final String format = String.format(event.getFormat(), player.getDisplayName(), message);

            @Override
            public void run() {
                try {
                    boolean isToxic;
                    String censoredMessage;
                    if(plugin.getCacheManager().isCached(message)) {
                        Cache cache = plugin.getCacheManager().getCache(message);
                        isToxic = cache.toxic();
                        censoredMessage = cache.censored();
                    }else {
                        Pair<String, Boolean> censoredPair = plugin.getPerspectiveAPI().censorAsync(message).get();
                        censoredMessage = censoredPair.getFirst();
                        isToxic = censoredPair.getSecond();
                    }


                    if(isToxic) {
                        MuteManager.warnPlayer(player.getUniqueId());

                        if(MuteManager.getWarnings(player.getUniqueId()) == 1) {
                            msg(player, plugin.getMessages().getWarned());
                        }else if(MuteManager.getWarnings(player.getUniqueId()) == 2) {
                            msg(player, plugin.getMessages().getWarned());
                            return;
                        }else if(MuteManager.getWarnings(player.getUniqueId()) >= 3) {
                            MuteManager.mutePlayer(player.getUniqueId());
                            MuteManager.clearWarnings(player.getUniqueId());
                            msg(player, plugin.getMessages().getMuted());
                            return;
                        }
                    }
                    plugin.getCacheManager().addCache(new Cache(message, censoredMessage, isToxic));
                    event.getRecipients().forEach(recipient -> {
                        if(!recipient.hasPermission("automod.staff"))
                            recipient.sendMessage(format.replace(message, censoredMessage));
                        else recipient.sendMessage(format);
                    });

                    ChatEvent chatEvent = new ChatEvent(player, format.replace(message, censoredMessage), format);
                    plugin.getRedisManager().publish(plugin.getGson().toJsonTree(chatEvent).getAsJsonObject());
                    this.cancel();
                }catch (InterruptedException | ExecutionException | URISyntaxException e) {
                    e.fillInStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void msg(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
