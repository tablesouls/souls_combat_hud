package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ResourceProviderRegistry {

    private final List<ResourceSource> sources = new ArrayList<>();

    public void register(ResourceSource source) {
        sources.add(source);
    }

    public ResourceSource resolve(Player player) {
        for (ResourceSource source : sources) {
            if (source.isAvailable(player)) {
                return source;
            }
        }
        return null;
    }
}