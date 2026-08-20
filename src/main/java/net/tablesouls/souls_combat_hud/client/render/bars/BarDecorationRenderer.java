package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

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

        int middleScissorMinX = leftCapX + tileSize;
        int middleScissorMaxX = rightCapX;

        if (middleScissorMaxX > middleScissorMinX) {
            // enableScissor works in absolute screen pixels and ignores the pose stack, so
            // local coordinates (which may live inside a scaled gauge overlay transform)
            // must be converted to real screen coordinates before use here.
            Vector3f screenMin = graphics.pose().last().pose()
                    .transformPosition(new Vector3f(middleScissorMinX, decorationY, 0));
            Vector3f screenMax = graphics.pose().last().pose()
                    .transformPosition(new Vector3f(middleScissorMaxX, decorationY + tileSize, 0));

            graphics.enableScissor(
                    Math.round(screenMin.x()), Math.round(screenMin.y()),
                    Math.round(screenMax.x()), Math.round(screenMax.y())
            );
            renderTiledMiddle(
                    graphics,
                    decoration.texture(),
                    x,
                    decorationY,
                    width,
                    tileSize,
                    decoration.middleU(),
                    decoration.middleV(),
                    tileSize,
                    textureWidth,
                    textureHeight
            );
            graphics.disableScissor();
        }

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

    private static void renderTiledMiddle(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height,
            int u,
            int v,
            int tileSize,
            int textureWidth,
            int textureHeight
    ) {
        int drawnWidth = 0;

        while (drawnWidth < width) {
            int sliceWidth = Math.min(tileSize, width - drawnWidth);

            graphics.blit(
                    texture,
                    x + drawnWidth,
                    y,
                    sliceWidth,
                    height,
                    u,
                    v,
                    sliceWidth,
                    tileSize,
                    textureWidth,
                    textureHeight
            );

            drawnWidth += sliceWidth;
        }
    }
}