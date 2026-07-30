package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.world.entity.player.Player;

public interface ResourceSource {
    boolean isAvailable(Player player);
    float getCurrent(Player player);
    float getMax(Player player);
}