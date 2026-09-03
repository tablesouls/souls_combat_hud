package net.tablesouls.souls_combat_hud.compat;

import net.tablesouls.souls_combat_hud.compat.legendarysurvivaloverhaul.LegendarySurvivalOverhaulCompat;
import net.tablesouls.souls_combat_hud.compat.legendarysurvivaloverhaul.LegendarySurvivalOverhaulThirstSource;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedCompat;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedThirstSource;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;

public final class ThirstSourceRegistry extends AbstractResourceSourceRegistry<ThirstSourceMode> {

    public static final ThirstSourceRegistry INSTANCE = new ThirstSourceRegistry();

    private ThirstSourceRegistry() {
        super(ThirstSourceMode.class);

        if (ThirstWasReclaimedCompat.LOADED) {
            register(new ThirstWasReclaimedThirstSource());
        }
        if (LegendarySurvivalOverhaulCompat.LOADED) {
            register(new LegendarySurvivalOverhaulThirstSource());
        }
    }
}