package net.tablesouls.souls_combat_hud.compat.legendarysurvivaloverhaul;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;
import sfiomn.legendarysurvivaloverhaul.api.thirst.IThirstAttachment;
import sfiomn.legendarysurvivaloverhaul.api.thirst.ThirstUtil;
import sfiomn.legendarysurvivaloverhaul.common.attachments.thirst.ThirstAttachment;
import sfiomn.legendarysurvivaloverhaul.util.AttachmentUtil;

public class LegendarySurvivalOverhaulThirstSource implements ResourceSource<ThirstSourceMode> {

    @Override
    public ThirstSourceMode mode() {
        return ThirstSourceMode.LEGENDARY_SURVIVAL_OVERHAUL;
    }

    @Override
    public boolean isAvailable(Player player) {
        return LegendarySurvivalOverhaulCompat.LOADED
                && ThirstUtil.isThirstActive(player)
                && resolveThirst(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        IThirstAttachment thirst = resolveThirst(player);
        return thirst != null ? thirst.getHydrationLevel() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        return ThirstAttachment.MAX_HYDRATION;
    }

    private static IThirstAttachment resolveThirst(Player player) {
        return AttachmentUtil.getThirstAttachment(player);
    }
}