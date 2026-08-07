package net.tablesouls.souls_combat_hud.mixin.preview;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererOffsetMixin {
    @Inject(method = "getRenderOffset", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeRenderOffset(
            AbstractClientPlayer entity, float partialTicks, CallbackInfoReturnable<Vec3> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}