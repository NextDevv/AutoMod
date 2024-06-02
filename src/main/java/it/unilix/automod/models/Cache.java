package it.unilix.automod.models;

import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;

import java.util.HashMap;

public record Cache(String message, String censored, boolean toxic) {
    public Cache(HashMap<String, Object> cacheMap) {
        this((String) cacheMap.get("message"), (String) cacheMap.get("censored"), Boolean.parseBoolean(cacheMap.get("toxic").toString()));
    }

    public Cache(LinkedTreeMap<String, Object> cacheMap) {
        this((String) cacheMap.get("message"), (String) cacheMap.get("censored"), Boolean.parseBoolean(cacheMap.get("toxic").toString()));
    }
}
