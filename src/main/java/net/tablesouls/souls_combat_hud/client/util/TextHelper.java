package net.tablesouls.souls_combat_hud.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

public class TextHelper {
    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    public static String toRomanNumeral(int number) {
        if (number >= 1 && number <= ROMAN_NUMERALS.length) {
            return ROMAN_NUMERALS[number - 1];
        }
        return String.valueOf(number);
    }

    public static void drawOutlinedString(
            GuiGraphics guiGraphics,
            Font font,
            Component text,
            int x,
            int y,
            int color,
            int outlineColor
    ) {
        guiGraphics.drawString(font, text, x - 1, y, outlineColor, false);
        guiGraphics.drawString(font, text, x + 1, y, outlineColor, false);
        guiGraphics.drawString(font, text, x, y - 1, outlineColor, false);
        guiGraphics.drawString(font, text, x, y + 1, outlineColor, false);

        guiGraphics.drawString(font, text, x, y, color, false);
    }

    public static int resolveTextBaseX(TextAnchor anchor, int boxX, int boxWidth, int textWidth) {
        return switch (anchor) {
            case CENTER -> boxX + boxWidth / 2 - textWidth / 2;
            case OUTSIDE_LEFT -> boxX - textWidth;
            case OUTSIDE_RIGHT -> boxX + boxWidth;
            case INSIDE_LEFT -> boxX;
            case INSIDE_RIGHT -> boxX + boxWidth - textWidth;
        };
    }
}