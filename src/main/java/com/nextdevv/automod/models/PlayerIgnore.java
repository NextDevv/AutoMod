package com.nextdevv.automod.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class PlayerIgnore {
    String uuid;
    ArrayList<String> ignoredPlayers;
    boolean ignoreAll;

    public PlayerIgnore() {

    }

    public PlayerIgnore(String uuid, ArrayList<String> ignoredPlayers, boolean ignoreAll) {
        this.uuid = uuid;
        this.ignoredPlayers = ignoredPlayers;
        this.ignoreAll = ignoreAll;
    }

    public String uuid() {
        return uuid;
    }

    public ArrayList<String> ignoredPlayers() {
        return ignoredPlayers;
    }

    public boolean ignoreAll() {
        return ignoreAll;
    }

    @Override
    public String toString() {
        return "PlayerIgnore{" +
                "uuid='" + uuid + '\'' +
                ", ignoredPlayers=" + ignoredPlayers +
                ", ignoreAll=" + ignoreAll +
                '}';
    }
}
