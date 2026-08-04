package net.tablesouls.souls_combat_hud.compat.parcool;

import com.alrex.parcool.api.Stamina;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

public class ParcoolStaminaSource implements ResourceSource<StaminaSourceMode> {

    @Override
    public StaminaSourceMode mode() {
        return StaminaSourceMode.PARCOOL;
    }

    @Override
    public boolean isAvailable(Player player) {
        return ParcoolCompat.LOADED && Stamina.get(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        Stamina stamina = Stamina.get(player);
        return stamina != null ? stamina.getValue() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        Stamina stamina = Stamina.get(player);
        return stamina !=null ? stamina.getMaxValue() : 0.0f;
    }
}
