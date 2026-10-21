package net.tablesouls.souls_combat_hud.compat.mna;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.resource.ICastingResource;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public class ManaAndArtificeManaSource implements ResourceSource<ManaSourceMode> {

    private static ICastingResource resource(Player player) {
        return player.getCapability(ManaAndArtificeMod.getMagicCapability())
                .resolve()
                .map(IPlayerMagic::getCastingResource)
                .orElse(null);
    }

    @Override
    public ManaSourceMode mode() {
        return ManaSourceMode.MANA_AND_ARTIFICE;
    }

    @Override
    public boolean isAvailable(Player player) {
        ICastingResource r = resource(player);
        return ManaAndArtificeCompat.LOADED && r != null && r.getMaxAmount() > 0;
    }

    @Override
    public float getCurrent(Player player) {
        ICastingResource r = resource(player);
        return r != null ? r.getAmount() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        ICastingResource r = resource(player);
        return r != null ? r.getMaxAmount() : 0.0f;
    }
}