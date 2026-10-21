package net.tablesouls.souls_combat_hud.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public class ArsNouveauManaSource implements ResourceSource<ManaSourceMode> {
    private static IManaCap cap(Player player) {
        return CapabilityRegistry.getMana(player).resolve().orElse(null);
    }

    @Override
    public ManaSourceMode mode() {
        return ManaSourceMode.ARS_NOUVEAU;
    }

    @Override
    public boolean isAvailable(Player player) {
        IManaCap c = cap(player);
        return ArsNouveauCompat.LOADED && c != null && c.getMaxMana() > 0;
    }

    @Override
    public float getCurrent(Player player) {
        IManaCap c = cap(player);
        return c != null ? (float) c.getCurrentMana() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        IManaCap c = cap(player);
        return c != null ? (float) c.getMaxMana() : 0.0f;
    }
}