package net.tablesouls.souls_combat_hud.compat;

import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightStaminaSource;
import net.tablesouls.souls_combat_hud.compat.paraglider.ParagliderCompat;
import net.tablesouls.souls_combat_hud.compat.paraglider.ParagliderStaminaSource;
import net.tablesouls.souls_combat_hud.compat.parcool.ParcoolCompat;
import net.tablesouls.souls_combat_hud.compat.parcool.ParcoolStaminaSource;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

public final class StaminaSourceRegistry extends AbstractResourceSourceRegistry<StaminaSourceMode> {

    public static final StaminaSourceRegistry INSTANCE = new StaminaSourceRegistry();

    private StaminaSourceRegistry() {
        super(StaminaSourceMode.class);

        if (EpicFightCompat.LOADED) {
            register(new EpicFightStaminaSource());
        }

        if (ParcoolCompat.LOADED) {
            register(new ParcoolStaminaSource());
        }

        if (ParagliderCompat.LOADED) {
            register(new ParagliderStaminaSource());
        }
    }
}