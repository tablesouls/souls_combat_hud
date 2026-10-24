package net.tablesouls.souls_combat_hud.compat;

import net.tablesouls.souls_combat_hud.compat.ars_nouveau.ArsNouveauCompat;
import net.tablesouls.souls_combat_hud.compat.ars_nouveau.ArsNouveauManaSource;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsManaSource;
import net.tablesouls.souls_combat_hud.compat.mineandslash.MineAndSlashCompat;
import net.tablesouls.souls_combat_hud.compat.mineandslash.MineAndSlashManaSource;
import net.tablesouls.souls_combat_hud.compat.mna.ManaAndArtificeCompat;
import net.tablesouls.souls_combat_hud.compat.mna.ManaAndArtificeManaSource;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public final class ManaSourceRegistry extends AbstractResourceSourceRegistry<ManaSourceMode> {

    public static final ManaSourceRegistry INSTANCE = new ManaSourceRegistry();

    private ManaSourceRegistry() {
        super(ManaSourceMode.class);

        if (IronsSpellsCompat.LOADED) {
            register(new IronsSpellsManaSource());
        }

        if (MineAndSlashCompat.LOADED) {
            register(new MineAndSlashManaSource());
        }

        if (ManaAndArtificeCompat.LOADED) {
            register(new ManaAndArtificeManaSource());
        }

        if (ArsNouveauCompat.LOADED) {
            register(new ArsNouveauManaSource());
        }
    }
}