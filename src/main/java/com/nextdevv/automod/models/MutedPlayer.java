package com.nextdevv.automod.models;

public record MutedPlayer(String uuid, long mutedUntil, String reason) {
}
