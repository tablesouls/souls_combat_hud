package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecorations;
import net.tablesouls.souls_combat_hud.client.render.bars.BarElement;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

public final class GaugeBarRenderer {
    public static void renderBar(
            GuiGraphics graphics, GaugeStyleRegistry registry,
            String key, BarStyle fallbackStyle, BarElement barElement,
            boolean available, int anchorX, int parentY,
            int width, int height, float fraction, boolean mirrored, boolean reductionEnabled
    ) {
        renderBar(graphics, registry, key, fallbackStyle, barElement, available, anchorX, parentY,
                width, height, fraction, mirrored, reductionEnabled, -1f, -1f);
    }

    public static void renderBar(
            GuiGraphics graphics, GaugeStyleRegistry registry,
            String key, BarStyle fallbackStyle, BarElement barElement,
            boolean available, int anchorX, int parentY,
            int width, int height, float fraction, boolean mirrored, boolean reductionEnabled,
            float currentValue, float maxValue
    ) {
        renderBar(graphics, registry, key, fallbackStyle, barElement, available, anchorX, parentY,
                width, height, fraction, mirrored, reductionEnabled, currentValue, maxValue, 1.0f);
    }

    public static void renderBar(
            GuiGraphics graphics, GaugeStyleRegistry registry,
            String key, BarStyle fallbackStyle, BarElement barElement,
            boolean available, int anchorX, int parentY,
            int width, int height, float fraction, boolean mirrored, boolean reductionEnabled,
            float currentValue, float maxValue, float tint
    ) {
        GaugeLayout layout = registry.getLayout(key, GaugeLayout.DEFAULT);
        if (!layout.enabled() || !available) return;

        BarStyle style = registry.getStyle(key, fallbackStyle);
        barElement.withDecoration(registry.getDecoration(key, BarDecorations.DEFAULT));

        int x = mirrored ? anchorX - layout.x() - width : anchorX + layout.x();

        Component valueText = null;
        if (maxValue >= 0f && SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.showValueText.get()) {
            valueText = Component.literal(Math.round(currentValue) + "/" + Math.round(maxValue));
        }

        barElement.render(
                graphics,
                style,
                x, parentY + layout.y(),
                width, height,
                fraction,
                null,
                valueText,
                mirrored,
                reductionEnabled,
                tint,
                maxValue,
                -1f,
                false);
    }
}