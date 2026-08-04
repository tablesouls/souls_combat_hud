package net.tablesouls.souls_combat_hud.compat;

import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsManaSource;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public final class ManaSourceRegistry extends AbstractResourceSourceRegistry<ManaSourceMode> {

    public static final ManaSourceRegistry INSTANCE = new ManaSourceRegistry();

    private ManaSourceRegistry() {
        super(ManaSourceMode.class);

        if (IronsSpellsCompat.LOADED) {
            register(new IronsSpellsManaSource());
        }
    }
}
