package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.util.Mth;

public final class BarScaling {
    public static int resolveWidth(
            float currentMax,
            float baselineMax,
            float projectedMax,
            double curveExponent,
            int minWidth,
            int maxWidth
    ) {
        if (currentMax <= 0.0F || projectedMax <= 0.0F) {
            return 0;
        }

        int baselineWidth = widthForValue(baselineMax, projectedMax, curveExponent, maxWidth);
        baselineWidth = Math.max(minWidth, baselineWidth);

        if (currentMax <= baselineMax) {
            return baselineWidth;
        }

        int resolved = widthForValue(currentMax, projectedMax, curveExponent, maxWidth);
        resolved = Math.max(resolved, baselineWidth);

        return Mth.clamp(resolved, minWidth, maxWidth);
    }

    private static int widthForValue(float value, float projectedMax, double curveExponent, int maxWidth) {
        float linearFraction = Mth.clamp(value / projectedMax, 0.0F, 1.0F);
        float curvedFraction = (float) Math.pow(linearFraction, curveExponent);
        return Mth.floor(curvedFraction * maxWidth);
    }
}