package net.tablesouls.souls_combat_hud.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPosePreviewMixin {
    @Inject(method = "getSwimAmount", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeSwimAmount(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(0f);
        }
    }
}