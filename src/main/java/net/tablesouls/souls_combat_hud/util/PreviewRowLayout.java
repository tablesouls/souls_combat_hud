package net.tablesouls.souls_combat_hud.util;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

public final class PreviewRowLayout {
    public static int slotCenterX(int startCenterX, int index, int slotSize, int gap, boolean growRight) {
        int step = slotSize + gap;
        return growRight ? startCenterX + index * step : startCenterX - index * step;
    }

    public static int rowWidth(int count, int slotSize, int gap) {
        if (count <= 0) {
            return 0;
        }
        return count * slotSize + (count - 1) * gap;
    }

    public static <T> void render(GuiGraphics guiGraphics, int startCenterX, int centerY, List<T> items, int maxSlots, int slotSize, int gap, boolean growRight, SlotRenderer<T> renderer) {
        int count = Math.min(items.size(), maxSlots);
        for (int i = 0; i < count; ++i) {
            int cx = slotCenterX(startCenterX, i, slotSize, gap, growRight);
            renderer.render(guiGraphics, items.get(i), cx, centerY);
        }
    }

    @FunctionalInterface
    public static interface SlotRenderer<T> {
        public void render(GuiGraphics guiGraphics, T item, int centerX, int centerY);
    }
}