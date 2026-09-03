package net.tablesouls.souls_combat_hud.compat.mineandslash;

import com.robertx22.mine_and_slash.capability.entity.EntityData;
import com.robertx22.mine_and_slash.saveclasses.unit.ResourceType;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public class MineAndSlashManaSource implements ResourceSource<ManaSourceMode> {

    @Override
    public ManaSourceMode mode() {
        return ManaSourceMode.MINE_AND_SLASH;
    }

    @Override
    public boolean isAvailable(Player player) {
        return MineAndSlashCompat.LOADED && EntityData.get(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        EntityData data = EntityData.get(player);
        return data != null ? data.getCurrentMana() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        EntityData data = EntityData.get(player);
        return data != null ? data.getMaximumResource(ResourceType.mana) : 0.0f;
    }
}