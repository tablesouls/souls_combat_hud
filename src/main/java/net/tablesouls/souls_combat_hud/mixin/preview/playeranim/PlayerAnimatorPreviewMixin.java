package net.tablesouls.souls_combat_hud.mixin.preview.playeranim;

import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.kosmx.playerAnim.core.impl.AnimationProcessor", remap = false)
public abstract class PlayerAnimatorPreviewMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$supress(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isRenderingPreview()) {
            cir.setReturnValue(false);
        }
    }
}
