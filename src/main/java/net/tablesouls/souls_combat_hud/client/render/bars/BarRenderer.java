package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class BarRenderer {

    public static void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float filledFraction, float revealedFraction, Component label, boolean mirrored, boolean reductionEnabled
    ) {
        render(graphics, style, x, y, w, h, filledFraction, revealedFraction, label, null, mirrored, reductionEnabled);
    }

    public static void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float filledFraction, float revealedFraction, Component label, Component valueText,
            boolean mirrored, boolean reductionEnabled
    ) {
        graphics.fill(x, y, x + w, y + h, style.barBgColor());

        int filled = filledFraction >= 1f ? w : Mth.floor(filledFraction * w);
        int revealedFilled = revealedFraction >= 1f ? w : Mth.floor(revealedFraction * w);

        if (reductionEnabled && revealedFilled > filled) {
            int lo = mirrored ? w - revealedFilled : filled;
            int hi = mirrored ? w - filled : revealedFilled;
            graphics.fill(x + lo, y, x + hi, y + h, style.barReductionColor());
        }

        if (filled > 0) {
            int lo = mirrored ? w - filled : 0;
            int hi = mirrored ? w : filled;
            graphics.fill(x + lo, y, x + hi, y + h, style.barColor());
        }

        if (label != null) {
            int nameY = y - 10;
            int labelX = mirrored ? x + w - Minecraft.getInstance().font.width(label) : x;
            graphics.drawString(Minecraft.getInstance().font, label, labelX, nameY, style.textColor(), true);
        }

        if (valueText != null) {
            Font font = Minecraft.getInstance().font;

            float textScale = 0.5f;
            float scaledTextWidth = font.width(valueText) * textScale;
            float valueX = mirrored ? x + w - scaledTextWidth : x + 4;
            float valueY = y + (h - font.lineHeight * textScale) / 2;

            graphics.pose().pushPose();
            graphics.pose().translate(valueX, valueY, 0);
            graphics.pose().scale(textScale, textScale, 1.0f);
            graphics.drawString(
                    font,
                    valueText,
                    0,
                    0,
                    style.textColor(),
                    true
            );
            graphics.pose().popPose();
        }
    }
}