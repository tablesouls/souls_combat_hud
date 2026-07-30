package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightStaminaSource;

public final class StaminaProviderRegistry {

    private static final ResourceProviderRegistry REGISTRY = new ResourceProviderRegistry();

    static {
        if (EpicFightCompat.LOADED) {
            REGISTRY.register(new EpicFightStaminaSource());
        }
    }

    public static ResourceSource resolve(Player player) {
        return REGISTRY.resolve(player);
    }
}