package net.tablesouls.souls_combat_hud.compat;

import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedCompat;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedSource;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;

public final class ThirstSourceRegistry extends AbstractResourceSourceRegistry<ThirstSourceMode> {

    public static final ThirstSourceRegistry INSTANCE = new ThirstSourceRegistry();

    private ThirstSourceRegistry() {
        super(ThirstSourceMode.class);

        if (ThirstWasReclaimedCompat.LOADED) {
            register(new ThirstWasReclaimedSource());
        }
    }
}