package it.unilix.automod.models;

import com.google.gson.internal.LinkedTreeMap;

public record Cache(String message, String censored, boolean toxic) {
    public Cache(LinkedTreeMap<String, Object> cacheMap) {
        this((String) cacheMap.get("message"), (String) cacheMap.get("censored"), Boolean.parseBoolean(cacheMap.get("toxic").toString()));
    }
}
