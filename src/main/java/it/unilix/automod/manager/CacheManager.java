package it.unilix.automod.manager;

import com.google.gson.internal.LinkedTreeMap;
import it.unilix.automod.AutoMod;
import it.unilix.automod.models.Cache;
import it.unilix.json.JsonFile;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    @Getter
    private final List<Cache> cacheList = new ArrayList<>();
    private @Nullable File folder = null;
    private final AutoMod plugin;

    public boolean folderInitialized() {
        return folder != null && folder.exists();
    }

    public CacheManager(AutoMod plugin) {
        this.plugin = plugin;
    }

    public void addCache(Cache cache) {
        if(cacheList.stream().anyMatch(c -> c.message().equals(cache.message())))
            return;
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
            ArrayList<?> cachesObj = (ArrayList<?>) file.getObj2("caches", ArrayList.class);
            ArrayList<Cache> caches = new ArrayList<>();

            for (Object obj : cachesObj) {
                LinkedTreeMap<String, Object> cacheMap = (LinkedTreeMap<String, Object>) obj;
                caches.add(new Cache(cacheMap));
            }

            cacheList.addAll(caches);
            plugin.getLogger().info("Loaded " + caches.size() + " cache(s).");
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
        file.set("caches", cacheList);
        file.save();
    }

    public boolean isCached(String message) {
        return cacheList.stream().anyMatch(cache -> cache.message().equals(message));
    }

    public Cache getCache(String message) {
        return cacheList.stream().filter(cache -> cache.message().equals(message)).findFirst().orElse(null);
    }
}
