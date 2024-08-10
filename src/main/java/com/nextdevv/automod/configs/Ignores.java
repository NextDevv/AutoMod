package com.nextdevv.automod.configs;

import com.nextdevv.automod.models.PlayerIgnore;
import it.unilix.yaml.YamlComment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Ignores {
    @YamlComment("List of players that are ignored by other players.")
    ArrayList<PlayerIgnore> ignores = new ArrayList<>();
}
