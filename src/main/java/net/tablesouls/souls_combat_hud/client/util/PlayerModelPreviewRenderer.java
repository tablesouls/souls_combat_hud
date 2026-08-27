package net.tablesouls.souls_combat_hud.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.item.ItemStack;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightAnimationFreezer;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerModelPreviewRenderer {
    private static final float BASE_YAW = 180f;
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

    public static boolean isPreviewHeldItem(ItemStack stack) {
        AbstractClientPlayer target = previewTarget;
        return renderingPreview && target != null && stack != null && target.getMainHandItem() == stack;
    }

    public static boolean isPreviewWalkAnimation(WalkAnimationState state) {
        return state != null && state == previewWalkAnimation;
    }

    public static boolean render(
            GuiGraphics graphics,
            AbstractClientPlayer player,
            int scissorX,
            int scissorY,
            int scissorSize,
            int modelX,
            int modelY,
            int modelSize,
            float rotationDegrees
    ) {
        int anchorX = modelX + modelSize / 2;
        int anchorY = modelY + (int) (modelSize * 1.6f);

        int modelScale = (int) (modelSize * 0.9f);

        float bodyYaw = BASE_YAW + rotationDegrees;

        renderingPreview = true;
        previewTarget = player;
        previewWalkAnimation = player.walkAnimation;

        float savedBodyRot = player.yBodyRot;
        float savedYRot = player.getYRot();
        float savedXRot = player.getXRot();
        float savedHeadRotO = player.yHeadRotO;
        float savedHeadRot = player.yHeadRot;

        EpicFightAnimationFreezer.LockedFacingSnapshot lockedFacing =
                EpicFightCompat.LOADED ? EpicFightAnimationFreezer.captureLockedFacing(player) : null;

        FrozenPose.Snapshot poseSnapshot = FrozenPose.freeze(player, rotationDegrees);

        EpicFightAnimationFreezer.FrozenAnimation animationSnapshot =
                EpicFightCompat.LOADED ? EpicFightAnimationFreezer.freezeToIdle(player) : null;

        try {
            player.yBodyRot = bodyYaw;
            player.setYRot(bodyYaw);
            player.setXRot(0f);
            player.xRotO = 0f;
            player.yHeadRot = bodyYaw;
            player.yHeadRotO = bodyYaw;

            int pad = 32;

            Vector3f screenOrigin = graphics.pose().last().pose().transformPosition(new Vector3f(scissorX, scissorY, 0));
            float ambientScale = graphics.pose().last().pose().m00();
            int scaledPad = Math.round(pad * ambientScale);
            int scaledSize = Math.round(scissorSize * ambientScale);
            int screenX = Math.round(screenOrigin.x());
            int screenY = Math.round(screenOrigin.y());

            graphics.enableScissor(
                    screenX - scaledPad,
                    screenY - scaledPad,
                    screenX + scaledSize + scaledPad,
                    screenY + scaledSize);

            Quaternionf entityRotation = new Quaternionf().rotateZ((float) Math.PI);

            InventoryScreen.renderEntityInInventory(
                    graphics,
                    anchorX, anchorY,
                    modelScale,
                    entityRotation,
                    null,
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
            player.yBodyRot = savedBodyRot;
            player.setYRot(savedYRot);
            player.setXRot(savedXRot);
            player.yHeadRotO = savedHeadRotO;
            player.yHeadRot = savedHeadRot;

            if (EpicFightCompat.LOADED) {
                EpicFightAnimationFreezer.restoreLockedFacingIfStillActive(lockedFacing);
                if (animationSnapshot != null) {
                    EpicFightAnimationFreezer.restore(player, animationSnapshot);
                }
            }

            FrozenPose.restore(player, poseSnapshot);

            renderingPreview = false;
            previewTarget = null;
            previewWalkAnimation = null;
            graphics.disableScissor();
        }
    }
}