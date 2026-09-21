package net.tablesouls.souls_combat_hud.client.render.status_gauge.thirst;

import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;

import java.util.EnumMap;
import java.util.Map;

public final class ThirstIconRegistry {
    private static final Map<ThirstSourceMode, ThirstIcon> ICONS = new EnumMap<>(ThirstSourceMode.class);

    static {
        ICONS.put(ThirstSourceMode.THIRST_WAS_RECLAIMED, new ThirstIcon(
                ResourceLocation.fromNamespaceAndPath("thirst", "textures/gui/thirst_icons.png"),
                25, 9,
                9,
                0,
                new int[]{0, 8, 16}
        ));
        ICONS.put(ThirstSourceMode.LEGENDARY_SURVIVAL_OVERHAUL, new ThirstIcon(
                ResourceLocation.fromNamespaceAndPath("legendarysurvivaloverhaul", "textures/gui/overlay.png"),
                256, 256,
                9,
                0,
                new int[]{0, 18, 9}
        ));
    }

    public static ThirstIcon get(ThirstSourceMode mode) {
        return mode == null ? null : ICONS.get(mode);
    }
}