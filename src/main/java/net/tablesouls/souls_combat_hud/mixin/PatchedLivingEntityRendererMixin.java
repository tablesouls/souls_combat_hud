package net.tablesouls.souls_combat_hud.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

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
}