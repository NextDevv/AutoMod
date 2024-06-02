package it.unilix.automod.manager;

import it.unilix.automod.AutoMod;
import it.unilix.automod.models.Cache;
import it.unilix.json.JsonFile;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheManager {
    @Getter
    private final List<Cache> cacheList = new ArrayList<>();
    private @Nullable File folder = null;
    private final AutoMod plugin;

    public CacheManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void addCache(Cache cache) {
        cacheList.add(cache);
    }

    public void clearCache() {
        cacheList.clear();
    }

    public void removeCache(String message) {
        cacheList.removeIf(cache -> cache.message().equals(message));
    }

    public void load() {
        folder = new File(plugin.getDataFolder().getAbsolutePath(), "cache");
        if (!folder.exists()) {
            boolean successful = folder.mkdirs();
            if (!successful) {
                throw new RuntimeException("Failed to create cache folder.");
            }
        }

        JsonFile file = new JsonFile(new File(folder, "cache.json"));
        if (!file.exists()) {
            file.createIfNotExists();
            return;
        }
        file.load();

        try {
            @SuppressWarnings("unchecked") ArrayList<Cache> caches = (ArrayList<Cache>) file.getFileObj(ArrayList.class);
            cacheList.addAll(caches);
            file.save();
        }catch (Exception e) {
            plugin.getLogger().severe("Failed to load cache file.");
        }
    }

    public void save() {
        if (folder == null) {
            throw new RuntimeException("Cache folder is not initialized.");
        }

        JsonFile file = new JsonFile(new File(folder, "cache.json"));
        file.createIfNotExists();
        file.setFileObj(cacheList);
        file.save();
    }

    public boolean isCached(String message) {
        return cacheList.stream().anyMatch(cache -> cache.message().equals(message));
    }

    public Cache getCache(String message) {
        return cacheList.stream().filter(cache -> cache.message().equals(message)).findFirst().orElse(null);
    }
}
