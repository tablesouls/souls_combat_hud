package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelPreviewMixin {
    @ModifyVariable(method = "setupAnim", at = @At("HEAD"), ordinal = 0)
    private float souls_combat_hud$freezeLimbSwing(float limbSwing, LivingEntity entity) {
        return PlayerModelPreviewRenderer.isPreviewTarget(entity) ? 0f : limbSwing;
    }

    @ModifyVariable(method = "setupAnim", at = @At("HEAD"), ordinal = 1)
    private float souls_combat_hud$freezeLimbSwingAmount(float limbSwingAmount, LivingEntity entity) {
        return PlayerModelPreviewRenderer.isPreviewTarget(entity) ? 0f : limbSwingAmount;
    }

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void souls_combat_hud$freezeCrouchField(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            ((HumanoidModel<?>) (Object) this).crouching = false;
        }
    }
}