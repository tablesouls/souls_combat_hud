package net.tablesouls.souls_combat_hud.compat.parcool;

import com.alrex.parcool.api.stamina.IReadableStamina;
import com.alrex.parcool.common.Parkourability;
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
        return ParcoolCompat.LOADED && Parkourability.get(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        Parkourability parkourability = Parkourability.get(player);
        if (parkourability == null) return 0.0f;
        IReadableStamina stamina = parkourability.getStamina();
        return stamina != null ? (float) stamina.value() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        Parkourability parkourability = Parkourability.get(player);
        if (parkourability == null) return 0.0f;
        IReadableStamina stamina = parkourability.getStamina();
        if (stamina == null) return 0.0f;
        float max = (float) stamina.max();
        return max > 0.0f ? max : (float) stamina.value();
    }
}