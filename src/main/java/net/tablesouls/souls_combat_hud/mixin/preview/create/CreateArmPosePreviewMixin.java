package net.tablesouls.souls_combat_hud.mixin.preview.create;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
        "com.simibubi.create.content.equipment.zapper.ZapperItem",
        "com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem"
}, remap = false)
public abstract class CreateArmPosePreviewMixin {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$noCustomArmPoseInPreview(
            ItemStack stack, AbstractClientPlayer player, InteractionHand hand,
            CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget(player)) {
            cir.setReturnValue(null);
        }
    }
}