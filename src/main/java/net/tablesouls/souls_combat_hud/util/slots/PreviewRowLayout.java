package net.tablesouls.souls_combat_hud.util.slots;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.tablesouls.souls_combat_hud.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.util.ElementOrientation;

public final class PreviewRowLayout {
    public static int rowLength(int count, int slotSize, int gap) {
        if (count <= 0) {
            return 0;
        }
        return count * slotSize + (count - 1) * gap;
    }

    public static <T> void render(
            GuiGraphics guiGraphics,
            int slotCenterX,
            int slotCenterY,
            int slotHalfW,
            int slotHalfH,
            List<T> items,
            int maxSlots,
            int slotSize,
            int gap,
            ElementAnchor previewAnchor,
            ElementOrientation orientation,
            SlotRenderer<T> renderer
    ) {
        int count = Math.min(items.size(), maxSlots);
        if (count == 0) {
            return;
        }

        int step = slotSize + gap;
        int previewHalf = slotSize / 2;

        if (orientation == ElementOrientation.HORIZONTAL) {
            int rowY = switch (previewAnchor.vertical()) {
                case TOP -> slotCenterY - slotHalfH - previewHalf - gap;
                case BOTTOM -> slotCenterY + slotHalfH + previewHalf + gap;
                case CENTER -> slotCenterY;
            };

            int firstX;
            int dx;
            switch (previewAnchor.horizontal()) {
                case RIGHT -> {
                    firstX = slotCenterX + slotHalfW + previewHalf + gap;
                    dx = 1;
                }
                case LEFT -> {
                    firstX = slotCenterX - slotHalfW - previewHalf - gap;
                    dx = -1;
                }
                default -> {
                    firstX = slotCenterX - rowLength(count, slotSize, gap) / 2 + previewHalf;
                    dx = 1;
                }
            }

            for (int i = 0; i < count; i++) {
                int cx = firstX + dx * i * step;
                renderer.render(guiGraphics, items.get(i), cx, rowY);
            }
        } else {
            int colX = switch (previewAnchor.horizontal()) {
                case LEFT -> slotCenterX - slotHalfW - previewHalf - gap;
                case RIGHT -> slotCenterX + slotHalfW + previewHalf + gap;
                case CENTER -> slotCenterX;
            };

            int firstY;
            int dy;
            switch (previewAnchor.vertical()) {
                case BOTTOM -> {
                    firstY = slotCenterY + slotHalfH + previewHalf + gap;
                    dy = 1;
                }
                case TOP -> {
                    firstY = slotCenterY - slotHalfH - previewHalf - gap;
                    dy = -1;
                }
                default -> {
                    firstY = slotCenterY - rowLength(count, slotSize, gap) / 2 + previewHalf;
                    dy = 1;
                }
            }

            for (int i = 0; i < count; i++) {
                int cy = firstY + dy * i * step;
                renderer.render(guiGraphics, items.get(i), colX, cy);
            }
        }
    }

    @FunctionalInterface
    public interface SlotRenderer<T> {
        void render(GuiGraphics guiGraphics, T item, int centerX, int centerY);
    }
}