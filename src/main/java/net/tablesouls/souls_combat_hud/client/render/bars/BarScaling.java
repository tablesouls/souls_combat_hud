package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.util.Mth;

public final class BarScaling {
    public static int resolveWidth(
            float currentMax,
            float baselineMax,
            float projectedMax,
            int minWidth,
            int maxWidth
    ) {
        if (currentMax <= 0.0F) {
            return 0;
        }

        int baselineWidth = Math.round(
                (baselineMax / projectedMax) * maxWidth
        );

        baselineWidth = Math.max(minWidth, baselineWidth);

        if (currentMax <= baselineMax) {
            return baselineWidth;
        }

        float fraction = (currentMax - baselineMax)
                / (projectedMax - baselineMax);

        fraction = Mth.clamp(fraction, 0.0F, 1.0F);

        return Mth.floor(
                Mth.lerp(
                        fraction,
                        baselineWidth,
                        maxWidth
                )
        );
    }
}