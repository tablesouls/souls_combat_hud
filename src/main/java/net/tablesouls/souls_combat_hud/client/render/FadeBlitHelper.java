package net.tablesouls.souls_combat_hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public final class FadeBlitHelper {
    public static void blitWithHorizontalFade(GuiGraphics graphics, ResourceLocation texture,
                                              int x, int y, int width, int height,
                                              float u0, float v0, float u1, float v1,
                                              int textureWidth, int textureHeight,
                                              int leftAlpha, int rightAlpha) {
        blitWithFade(graphics, texture, x, y, width, height, u0, v0, u1, v1,
                textureWidth, textureHeight, leftAlpha, leftAlpha, rightAlpha, rightAlpha);
    }

    public static void blitWithVerticalFade(GuiGraphics graphics, ResourceLocation texture,
                                            int x, int y, int width, int height,
                                            float u0, float v0, float u1, float v1,
                                            int textureWidth, int textureHeight,
                                            int topAlpha, int bottomAlpha) {
        blitWithFade(graphics, texture, x, y, width, height, u0, v0, u1, v1,
                textureWidth, textureHeight, topAlpha, bottomAlpha, bottomAlpha, topAlpha);
    }

    public static void blitWithFade(GuiGraphics graphics, ResourceLocation texture,
                                    int x, int y, int width, int height,
                                    float u0, float v0, float u1, float v1,
                                    int textureWidth, int textureHeight,
                                    int alphaTopLeft, int alphaBottomLeft,
                                    int alphaBottomRight, int alphaTopRight) {

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float minU = u0 / textureWidth;
        float maxU = u1 / textureWidth;
        float minV = v0 / textureHeight;
        float maxV = v1 / textureHeight;

        buffer.vertex(matrix, x, y, 0)
                .uv(minU, minV).color(255, 255, 255, alphaTopLeft).endVertex();
        buffer.vertex(matrix, x, y + height, 0)
                .uv(minU, maxV).color(255, 255, 255, alphaBottomLeft).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0)
                .uv(maxU, maxV).color(255, 255, 255, alphaBottomRight).endVertex();
        buffer.vertex(matrix, x + width, y, 0)
                .uv(maxU, minV).color(255, 255, 255, alphaTopRight).endVertex();

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
}