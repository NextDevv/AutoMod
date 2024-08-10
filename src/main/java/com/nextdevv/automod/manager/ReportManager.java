package com.nextdevv.automod.manager;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.events.ReportEvent;
import com.nextdevv.automod.utils.DiscordWebhook;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.nextdevv.automod.manager.MuteManager.handleDiscordIntegration;
import static com.nextdevv.automod.utils.ChatUtils.msg;

public class ReportManager {
    @Getter private final List<ReportEvent> reports = new ArrayList<>();
    private final AutoMod plugin;

    public ReportManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void add(ReportEvent report) {
        reports.add(report);
    }

    public void remove(ReportEvent report) {
        reports.remove(report);
    }

    public void notify(ReportEvent report) {
        String[] format = plugin.getSettings().getReportMessageFormat();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.hasPermission("automod.staff") || player.hasPermission("automod.reports")) {
                for (String s : format) {
                    String message = s.replace("{player}", report.reporter())
                            .replace("{target}", report.player())
                            .replace("{message}", report.reason());
                    msg(player, message);
                }
            }
        });
        handleDiscordIntegrationReport("Report", Color.RED, "Player " + report.reporter() + " reported " + report.player() + " for " + report.reason());
    }

    public void handleDiscordIntegrationReport(String title, Color color, String description) {
        if (plugin.getSettings().isDiscordIntegration()) {
            try {
                DiscordWebhook webhook = plugin.getReportDiscordWebhook();
                webhook.clearEmbeds();
                webhook.setUsername("AutoMod");
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
                embed.setTitle(title);
                embed.setColor(color);
                embed.setDescription(description);
                webhook.addEmbed(embed);
                webhook.execute();
            }catch (Exception e) {
                plugin.getLogger().severe("Failed to send Discord message: " + e.getMessage());
            }
        }
    }
}
