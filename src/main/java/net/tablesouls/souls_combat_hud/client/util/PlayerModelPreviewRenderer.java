package net.tablesouls.souls_combat_hud.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.WalkAnimationState;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightAnimationFreezer;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerModelPreviewRenderer {
    private static final float MODEL_YAW_BIAS = 50f;
    private static final long FAILURE_COOLDOWN_MS = 3000L;

    private static boolean renderingPreview = false;
    private static volatile AbstractClientPlayer previewTarget = null;

    private static volatile WalkAnimationState previewWalkAnimation = null;

    private static final Map<UUID, Long> skipUntil = new ConcurrentHashMap<>();

    public static boolean isRenderingPreview() {
        return renderingPreview;
    }

    public static boolean isPreviewTarget(Entity entity) {
        return entity != null && entity == previewTarget;
    }

    public static boolean isPreviewWalkAnimation(WalkAnimationState state) {
        return state != null && state == previewWalkAnimation;
    }

    public static boolean canRender() {
        return !EpicFightCompat.isComputeShaderActive();
    }

    public static boolean isSafeToRender(AbstractClientPlayer player) {
        if (!EpicFightCompat.LOADED) {
            return true;
        }

        UUID id = player.getUUID();
        long now = System.currentTimeMillis();
        Long until = skipUntil.get(id);
        if (until != null) {
            if (until > now) {
                return false;
            }
            skipUntil.remove(id);
        }

        boolean safe = EpicFightAnimationFreezer.isBaseLayerSafeToRender(player);

        if (!safe) {
            SoulsCombatHUD.LOGGER.warn(
                    "Skipping player model preview for {} (holding {}): base animation layer is empty",
                    player.getGameProfile().getName(),
                    player.getMainHandItem()
            );
            skipUntil.put(id, now + FAILURE_COOLDOWN_MS);
        }

        return safe;
    }

    public static boolean render(
            GuiGraphics graphics,
            AbstractClientPlayer player,
            int x,
            int y,
            int size,
            boolean faceRight
    ) {
        int anchorX = x + size / 2;
        int anchorY = y + (int) (size * 1.6f);

        int modelScale = (int) (size * 0.9f);

        float lookAtX = faceRight ? -MODEL_YAW_BIAS : MODEL_YAW_BIAS;
        float lookAtY = 0f;

        float angleX = (float) Math.atan(lookAtX / 40.0);
        float angleY = (float) Math.atan(lookAtY / 40.0);

        FrozenPose.Snapshot poseSnapshot = null;
        Object efSnapshot = null;
        renderingPreview = true;
        previewTarget = player;
        previewWalkAnimation = player.walkAnimation;

        try {
            poseSnapshot = FrozenPose.freeze(player, angleX);
            efSnapshot = EpicFightCompat.LOADED ? EpicFightAnimationFreezer.freezeToIdle(player) : null;

            if (EpicFightCompat.LOADED && !EpicFightAnimationFreezer.isBaseLayerSafeToRender(player)) {
                throw new IllegalStateException(
                        "Post-freeze animation state unsafe to render (holding " + player.getMainHandItem() + ")");
            }

            int pad = 32;

            graphics.enableScissor(
                    x - pad,
                    y - pad,
                    x + size + pad,
                    y + size);

            InventoryScreen.renderEntityInInventoryFollowsAngle(
                    graphics,
                    anchorX, anchorY,
                    modelScale,
                    angleX, angleY,
                    player
            );

            graphics.flush();
            RenderSystem.clearDepth(1.0D);
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
            return true;
        } catch (Throwable t) {
            SoulsCombatHUD.LOGGER.error(
                    "Player model preview render failed for {} (holding {}), falling back for this frame",
                    player.getGameProfile().getName(),
                    player.getMainHandItem(),
                    t
            );
            skipUntil.put(player.getUUID(), System.currentTimeMillis() + FAILURE_COOLDOWN_MS);
            return false;
        } finally {
            renderingPreview = false;
            previewTarget = null;
            previewWalkAnimation = null;
            if (EpicFightCompat.LOADED) {
                EpicFightAnimationFreezer.restore(player, (EpicFightAnimationFreezer.FrozenAnimation) efSnapshot);
            }
            if (poseSnapshot != null) {
                FrozenPose.restore(player, poseSnapshot);
            }
            graphics.disableScissor();
        }
    }
}