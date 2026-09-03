package net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed;

import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;

public class ThirstWasReclaimedThirstSource implements ResourceSource<ThirstSourceMode> {
    private static final float MAX_THIRST = 20.0f;

    @Override
    public ThirstSourceMode mode() {
        return ThirstSourceMode.THIRST_WAS_RECLAIMED;
    }

    @Override
    public boolean isAvailable(Player player) {
        return ThirstWasReclaimedCompat.LOADED && resolveThirst(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        IThirst thirst = resolveThirst(player);
        return thirst != null ? thirst.getThirst() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        return MAX_THIRST;
    }

    private static IThirst resolveThirst(Player player) {
        return player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
    }
}