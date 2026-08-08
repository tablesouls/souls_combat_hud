package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.tablesouls.souls_combat_hud.client.util.TextAnchor;
import net.tablesouls.souls_combat_hud.client.util.TextHelper;

public final class BarRenderer {

    public static void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float filledFraction, float revealedFraction, Component label, boolean mirrored, boolean reductionEnabled
    ) {
        render(graphics, style, x, y, w, h, filledFraction, revealedFraction, label, null, null, mirrored, reductionEnabled, true);
    }

    public static void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float filledFraction, float revealedFraction, Component label, Component valueText, Component damageText,
            boolean mirrored, boolean reductionEnabled
    ) {
        render(graphics, style, x, y, w, h, filledFraction, revealedFraction, label, valueText, damageText, mirrored, reductionEnabled, true);
    }

    public static void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float filledFraction, float revealedFraction, Component label, Component valueText, Component damageText,
            boolean mirrored, boolean reductionEnabled, boolean showDamageText
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
            TextAnchor labelAnchor = mirrored ? TextAnchor.INSIDE_RIGHT : TextAnchor.INSIDE_LEFT;
            int labelX = TextHelper.resolveTextBaseX(labelAnchor, x, w, Minecraft.getInstance().font.width(label));
            graphics.drawString(Minecraft.getInstance().font, label, labelX, nameY, style.textColor(), true);
        }

        if (damageText != null && showDamageText) {
            int damageY = y - 10;
            TextAnchor damageAnchor = mirrored ? TextAnchor.INSIDE_LEFT : TextAnchor.INSIDE_RIGHT;
            int damageX = TextHelper.resolveTextBaseX(damageAnchor, x, w, Minecraft.getInstance().font.width(damageText));
            graphics.drawString(Minecraft.getInstance().font, damageText, damageX, damageY, style.textColor(), true);
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