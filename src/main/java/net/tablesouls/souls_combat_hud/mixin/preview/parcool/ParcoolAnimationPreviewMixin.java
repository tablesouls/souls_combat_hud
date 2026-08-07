package net.tablesouls.souls_combat_hud.mixin.preview.parcool;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.alrex.parcool.common.capability.Animation", remap = false)
public abstract class ParcoolAnimationPreviewMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true, remap = false)
    private static void souls_combat_hud$suppressForPreview(Player player, CallbackInfoReturnable<Object> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(player)) {
            cir.setReturnValue(null);
        }
    }
}