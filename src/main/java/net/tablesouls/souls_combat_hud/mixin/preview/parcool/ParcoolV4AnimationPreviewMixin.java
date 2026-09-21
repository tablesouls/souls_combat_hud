package net.tablesouls.souls_combat_hud.mixin.preview.parcool;

import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.alrex.parcool.client.animation.system.PlayerAnimator", remap = false)
public abstract class ParcoolV4AnimationPreviewMixin {

    @Inject(method = "getCurrentTransformation", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$suppressForPreview(CallbackInfoReturnable<Object> cir) {
        if (PlayerModelPreviewRenderer.isRenderingPreview()) {
            cir.setReturnValue(null);
        }
    }
}