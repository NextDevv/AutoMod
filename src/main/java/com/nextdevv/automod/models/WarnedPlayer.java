package com.nextdevv.automod.models;

public record WarnedPlayer(String uuid, int warns, long warnedAt, String reason) {
}
