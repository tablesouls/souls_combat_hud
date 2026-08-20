package net.tablesouls.souls_combat_hud.mixin.preview;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPosePreviewMixin {
    @Inject(method = "getSwimAmount", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeSwimAmount(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(method = "isUsingItem", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeUsingItem(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getUseItemRemainingTicks", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeUseItemRemainingTicks(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getUseItem", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeUseItemStack(CallbackInfoReturnable<ItemStack> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "isFallFlying", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeFallFlying(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isAutoSpinAttack", at = @At("RETURN"), cancellable = true)
    private void souls_combat_hud$freezeSpinAttack(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModelPreviewRenderer.isPreviewTarget((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}