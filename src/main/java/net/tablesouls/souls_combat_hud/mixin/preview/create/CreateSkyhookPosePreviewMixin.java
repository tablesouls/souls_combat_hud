package net.tablesouls.souls_combat_hud.mixin.preview.create;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.simibubi.create.foundation.render.PlayerSkyhookRenderer", remap = false)
public abstract class CreateSkyhookPosePreviewMixin {

    @Inject(method = "beforeSetupAnim", at = @At("HEAD"), cancellable = true, remap = false)
    private static void souls_combat_hud$resetInsteadOfHang(Player player, HumanoidModel<?> model, CallbackInfo ci) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(player)) {
            model.head.resetPose();
            model.hat.resetPose();
            model.body.resetPose();
            model.rightArm.resetPose();
            model.leftArm.resetPose();
            model.rightLeg.resetPose();
            model.leftLeg.resetPose();
            ci.cancel();
        }
    }

    @Inject(method = "afterSetupAnim", at = @At("HEAD"), cancellable = true, remap = false)
    private static void souls_combat_hud$skipHangingPose(Player player, HumanoidModel<?> model, CallbackInfo ci) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(player)) {
            ci.cancel();
        }
    }
}