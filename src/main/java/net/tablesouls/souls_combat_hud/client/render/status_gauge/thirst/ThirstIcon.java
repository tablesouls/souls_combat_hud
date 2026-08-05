package net.tablesouls.souls_combat_hud.client.render.status_gauge.thirst;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public record ThirstIcon(
        ResourceLocation texture,
        int textureWidth,
        int textureHeight,
        int iconSize,
        int v,
        int[] stageU
) {
    public int stageForPercent(float percent) {
        if (stageU.length == 0) return 0;
        int index = Math.round(Mth.clamp(percent, 0.0f, 1.0f) * (stageU.length - 1));
        return Mth.clamp(index, 0, stageU.length - 1);
    }
}