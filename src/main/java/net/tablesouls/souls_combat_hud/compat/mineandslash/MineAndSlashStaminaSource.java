package net.tablesouls.souls_combat_hud.compat.mineandslash;

import com.robertx22.mine_and_slash.capability.entity.EntityData;
import com.robertx22.mine_and_slash.saveclasses.unit.ResourceType;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

public class MineAndSlashStaminaSource implements ResourceSource<StaminaSourceMode> {

    @Override
    public StaminaSourceMode mode() {
        return StaminaSourceMode.MINE_AND_SLASH;
    }

    @Override
    public boolean isAvailable(Player player) {
        return MineAndSlashCompat.LOADED && EntityData.get(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        EntityData data = EntityData.get(player);
        return data != null ? data.getResources().getEnergy() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        EntityData data = EntityData.get(player);
        return data != null ? data.getMaximumResource(ResourceType.energy) : 0.0f;
    }
}