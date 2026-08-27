package net.tablesouls.souls_combat_hud.mixin.preview.notenoughanimations;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.tr7zw.notenoughanimations.logic.PlayerTransformer", remap = false)
public abstract class NotEnoughAnimationsPreviewMixin {

    @Inject(method = "updateModel", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$suppressUpdateModelInPreview(
            AbstractClientPlayer entity, PlayerModel model, float swing, CallbackInfo info, CallbackInfo ci) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "preUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$suppressPreUpdateInPreview(
            AbstractClientPlayer entity, PlayerModel model, float swing, CallbackInfo info, CallbackInfo ci) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(entity)) {
            ci.cancel();
        }
    }
}