package com.nextdevv.automod.api;

import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.configs.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VpnProxyDetector {
    private final AutoMod plugin;
    private final HttpClient client;

    public VpnProxyDetector(AutoMod plugin) {
        this.plugin = plugin;
        this.client = HttpClient.newHttpClient();
    }

    public CompletableFuture<Boolean> isVpnProxy(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            Settings settings = plugin.getSettings();

            if (!settings.isVpnDetection()) return false;

            String oFlags = settings.getOflags() != '_' ? "&oflags=" + settings.getOflags() : "";

            HttpRequest request;
            try {
                request = HttpRequest.newBuilder()
                        .uri(new URI("https://check.getipintel.net/check.php?ip=" + ip + "&contact=" + settings.getContactEmail()
                                + "&flags=" + settings.getFlags() + oFlags))
                        .POST(HttpRequest.BodyPublishers.ofString("{\"ip\":\"" + ip + "\"}"))
                        .build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            try {
                double response = Double.parseDouble(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
                if (settings.isVpnOutput() || settings.isDebug()) {
                    plugin.getLogger().info("IP: " + ip + " | Response: " + response);
                }

                return response > settings.getThreshold();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        });
    }
}