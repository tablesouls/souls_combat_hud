package net.tablesouls.souls_combat_hud.mixin.preview;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerPreviewMixin {
    @Inject(method = "isCrouching", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeCrouching(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
