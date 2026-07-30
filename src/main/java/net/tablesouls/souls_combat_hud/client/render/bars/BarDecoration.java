package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.resources.ResourceLocation;

public record BarDecoration(
        ResourceLocation texture,
        int tileSize,
        int textureWidth,
        int textureHeight,

        int leftU,
        int middleU,
        int rightU,
        int progressU,

        int leftV,
        int middleV,
        int rightV,
        int progressV
) {
    public BarDecoration withTexture(ResourceLocation newTexture) {
        return new BarDecoration(newTexture, tileSize, textureWidth, textureHeight,
                leftU, middleU, rightU, progressU, leftV, middleV, rightV, progressV);
    }
}