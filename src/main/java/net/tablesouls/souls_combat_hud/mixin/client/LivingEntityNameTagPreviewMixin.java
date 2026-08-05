package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityNameTagPreviewMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void souls_combat_hud$hideNameTagInPreview(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            cir.setReturnValue(false);
        }
    }
}