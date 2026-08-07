package net.tablesouls.souls_combat_hud.mixin.preview;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = PatchedLivingEntityRenderer.class)
public abstract class PatchedLivingEntityRendererMixin {
    @Redirect(
            method = "mulPoseStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isCrouching()Z")
    )
    private boolean souls_combat_hud$suppressCrouchOffset(LivingEntity entity) {
        return !PlayerModelPreviewRenderer.isPreviewTarget(entity) && entity.isCrouching();
    }

    @Inject(method = "getOverlayCoord", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$hideDamageTintInPreview(
            LivingEntity entity, LivingEntityPatch<?> entityPatch, float partialTick,
            CallbackInfoReturnable<Integer> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            cir.setReturnValue(OverlayTexture.NO_OVERLAY);
        }
    }
}