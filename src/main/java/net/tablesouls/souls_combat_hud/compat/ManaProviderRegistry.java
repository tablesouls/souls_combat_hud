package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsManaSource;

public final class ManaProviderRegistry {

    private static final ResourceProviderRegistry REGISTRY = new ResourceProviderRegistry();

    static {
        if (IronsSpellsCompat.LOADED) {
            REGISTRY.register(new IronsSpellsManaSource());
        }
    }

    public static ResourceSource resolve(Player player) {
        return REGISTRY.resolve(player);
    }
}