package com.nextdevv.automod.manager;

import com.google.gson.internal.LinkedTreeMap;
import com.nextdevv.automod.AutoMod;
import com.nextdevv.automod.models.Cache;
import it.unilix.json.JsonFile;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllBytes;

public class CacheManager {
    @Getter
    private final List<Cache> cacheList = new ArrayList<>();
    private @Nullable File folder = null;
    private long lastDataSave = 0;
    private final AutoMod plugin;

    public CacheManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public boolean folderInitialized() {
        return folder != null && folder.exists();
    }

    public void addCache(Cache cache) {
        if (cacheList.stream().noneMatch(c -> c.message().equals(cache.message()))) {
            cacheList.add(cache);
        }
    }

    public void clearCache() {
        cacheList.clear();
    }

    public void load() {
        folder = new File(plugin.getDataFolder(), "cache");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Failed to create cache folder.");
        }

        JsonFile file = new JsonFile(new File(folder, "cache.json"));
        if (!file.exists()) {
            file.createIfNotExists();
            return;
        }
        file.load();

        try {
            ArrayList<?> cachesObj = (ArrayList<?>) file.getObj2("caches", ArrayList.class);
            for (Object obj : cachesObj) {
                @SuppressWarnings("unchecked")
                LinkedTreeMap<String, Object> cacheMap = (LinkedTreeMap<String, Object>) obj;
                cacheList.add(new Cache(cacheMap));
            }
            plugin.getLogger().info("Loaded " + cacheList.size() + " cache(s).");
            file.save();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load cache file.");
        }

        File lastSave = getSaveFile();
        try {
            lastDataSave = Long.parseLong(new String(readAllBytes(lastSave.toPath())));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to read lastSave file.");
            lastDataSave = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastDataSave > plugin.getSettings().getCacheExpireDays() * 86400000L) {
            clearCache();
            save();
            if (!lastSave.delete()) {
                throw new RuntimeException("Failed to delete lastSave file.");
            }
            plugin.getLogger().warning("Cache expired. Cleared all caches.");
        }
    }

    private File getSaveFile() {
        File lastSave = new File(folder, "lastSave");
        if (!lastSave.exists()) {
            try {
                if (!lastSave.createNewFile()) {
                    throw new RuntimeException("Failed to create lastSave file.");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create lastSave file.");
            }
        }
        return lastSave;
    }

    public void save() {
        if (folder == null) {
            throw new RuntimeException("Cache folder is not initialized.");
        }

        JsonFile file = new JsonFile(new File(folder, "cache.json"));
        file.createIfNotExists();
        file.set("caches", cacheList);
        file.save();

        lastDataSave = System.currentTimeMillis();
        File lastSave = getSaveFile();
        try (FileWriter writer = new FileWriter(lastSave)) {
            writer.write(String.valueOf(lastDataSave));
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write lastSave file.");
        }
    }

    public boolean isCached(String message) {
        return cacheList.stream().anyMatch(cache -> cache.message().equals(message));
    }

    public Cache getCache(String message) {
        return cacheList.stream().filter(cache -> cache.message().equals(message)).findFirst().orElse(null);
    }

    public boolean isLoaded() {
        return folder != null;
    }
}