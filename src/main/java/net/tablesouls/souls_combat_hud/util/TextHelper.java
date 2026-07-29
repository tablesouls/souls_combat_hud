package net.tablesouls.souls_combat_hud.util;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

public class TextHelper {
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
}
