package it.unilix.automod.models;

import lombok.Getter;

import java.util.HashMap;

public record Cache(String message, String censored, boolean toxic) {
    public Cache(HashMap<String, Object> cacheMap) {
        this((String) cacheMap.get("message"), (String) cacheMap.get("censored"), Boolean.parseBoolean(cacheMap.get("toxic").toString()));
    }
}
