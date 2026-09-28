package net.tablesouls.souls_combat_hud.mixin.preview;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ElytraModel.class, priority = 2000)
public abstract class ElytraModelMixin {
    @Shadow @Final private ModelPart leftWing;
    @Shadow @Final private ModelPart rightWing;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("RETURN"),
            require = 0
    )
    private void souls_combat_hud$idleElytraInPreview(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!PlayerModelPreviewRenderer.isPreviewTarget(entity)) return;

        final float rest = 0.2617994F;
        leftWing.y = 0f;
        leftWing.z = 0f;
        leftWing.xRot = rest;
        leftWing.yRot = 0f;
        leftWing.zRot = -rest;

        rightWing.y = 0f;
        rightWing.z = 0f;
        rightWing.xRot = rest;
        rightWing.yRot = 0f;
        rightWing.zRot = rest;
    }
}