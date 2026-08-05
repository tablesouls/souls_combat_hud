package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPosePreviewMixin {
    @Inject(method = "getPose", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezePose(CallbackInfoReturnable<Pose> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(Pose.STANDING);
        }
    }
}