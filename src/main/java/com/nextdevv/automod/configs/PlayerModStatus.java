package com.nextdevv.automod.configs;

import com.nextdevv.automod.models.MutedPlayer;
import com.nextdevv.automod.models.WarnedPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlayerModStatus {
    List<MutedPlayer> mutedPlayers = List.of();
    List<WarnedPlayer> warnedPlayers = List.of();
}
