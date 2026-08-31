package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BarDecorationRenderer {
    public static void render(
            GuiGraphics graphics, BarDecoration decoration, int x, int y,
            int width, int height, float fraction, boolean mirrored
    ) {
        int tileSize = decoration.tileSize();
        int textureWidth = decoration.textureWidth();
        int textureHeight = decoration.textureHeight();
        int decorationY = y + height / 2 - tileSize / 2;

        int overlap = tileSize / 2;

        int leftCapX = x - tileSize + overlap;
        int rightCapX = x + width - overlap;

        int filledWidth = Math.round(width * fraction);
        int progressX = mirrored
                ? x + (width - filledWidth) - tileSize / 2
                : x + filledWidth - tileSize / 2;

        renderTile(
                graphics,
                decoration,
                progressX,
                decorationY,
                decoration.progressU(),
                decoration.progressV()
        );

        int middleMinX = leftCapX + tileSize;
        int middleMaxX = rightCapX;

        renderTiledMiddleClipped(
                graphics,
                decoration.texture(),
                x,
                decorationY,
                middleMinX,
                middleMaxX,
                decoration.middleU(),
                decoration.middleV(),
                tileSize,
                textureWidth,
                textureHeight
        );

        renderTile(
                graphics,
                decoration,
                leftCapX,
                decorationY,
                decoration.leftU(),
                decoration.leftV()
        );

        renderTile(
                graphics,
                decoration,
                rightCapX,
                decorationY,
                decoration.rightU(),
                decoration.rightV()
        );
    }

    private static void renderTile(
            GuiGraphics graphics,
            BarDecoration decoration,

            int x,
            int y,

            int u,
            int v
    ) {
        graphics.blit(
                decoration.texture(),

                x,
                y,

                decoration.tileSize(),
                decoration.tileSize(),

                u,
                v,

                decoration.tileSize(),
                decoration.tileSize(),

                decoration.textureWidth(),
                decoration.textureHeight()
        );
    }

    private static void renderTiledMiddleClipped(
            GuiGraphics graphics,
            ResourceLocation texture,
            int anchorX,
            int y,
            int minX,
            int maxX,
            int u,
            int v,
            int tileSize,
            int textureWidth,
            int textureHeight
    ) {
        if (maxX <= minX) return;

        int offsetFromAnchor = minX - anchorX;
        int tileIndexStart = Math.floorDiv(offsetFromAnchor, tileSize);
        int tileStartX = anchorX + tileIndexStart * tileSize;

        int drawX = minX;
        while (drawX < maxX) {
            int tileLocalStart = drawX - tileStartX;
            int remainingInTile = tileSize - tileLocalStart;
            int sliceWidth = Math.min(remainingInTile, maxX - drawX);

            graphics.blit(
                    texture,
                    drawX,
                    y,
                    sliceWidth,
                    tileSize,
                    u + tileLocalStart,
                    v,
                    sliceWidth,
                    tileSize,
                    textureWidth,
                    textureHeight
            );

            drawX += sliceWidth;
            tileStartX += tileSize;
        }
    }
}