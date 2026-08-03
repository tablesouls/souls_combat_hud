package net.tablesouls.souls_combat_hud.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class PlayerFacePreviewRenderer {
    private static final int SKIN_TEX_SIZE = 64;
    private static final int FACE_U = 8, FACE_V = 8;
    private static final int FACE_TEX_SIZE = 8;
    private static final int FACE_LAYER_U = 40, FACE_LAYER_V = 8;

    private PlayerFacePreviewRenderer() {}

    public static void render(GuiGraphics graphics, ResourceLocation skinTexture, int x, int y, int size) {
        if (skinTexture == null) return;

        graphics.blit(skinTexture, x, y, size, size, FACE_U, FACE_V, FACE_TEX_SIZE, FACE_TEX_SIZE, SKIN_TEX_SIZE, SKIN_TEX_SIZE);

        PoseStack pose = graphics.pose();
        pose.pushPose();
        float hatLayerScale = 1.06f;
        pose.translate(x + size / 2f, y + size / 2f, 0);
        pose.scale(hatLayerScale, hatLayerScale, 1f);
        pose.translate(-(x + size / 2f), -(y + size / 2f), 0);
        graphics.blit(skinTexture, x, y, size, size, FACE_LAYER_U, FACE_LAYER_V, FACE_TEX_SIZE, FACE_TEX_SIZE, SKIN_TEX_SIZE, SKIN_TEX_SIZE);
        pose.popPose();
    }
}