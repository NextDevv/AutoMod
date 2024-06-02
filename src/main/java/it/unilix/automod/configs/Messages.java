package it.unilix.automod.configs;

import lombok.Getter;

@Getter
public class Messages {
    String muted = "You are muted!";
    String unmuted = "You are no longer muted!";
    String warned = "You have been warned!";
    String usage = "&cUsage: /automod <subcommand> [args]";
    String subNotFound = "&cSubcommand not found!";
}
