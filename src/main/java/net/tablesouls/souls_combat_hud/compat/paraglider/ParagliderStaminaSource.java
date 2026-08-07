package net.tablesouls.souls_combat_hud.compat.paraglider;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import tictim.paraglider.api.stamina.Stamina;

public class ParagliderStaminaSource implements ResourceSource<StaminaSourceMode> {
    @Override
    public StaminaSourceMode mode() {
        return StaminaSourceMode.PARAGLIDER;
    }

    @Override
    public boolean isAvailable(Player player) {
        return ParagliderCompat.LOADED;
    }

    @Override
    public float getCurrent(Player player) {
        return Stamina.get(player).stamina();
    }

    @Override
    public float getMax(Player player) {
        return Stamina.get(player).maxStamina();
    }
}
