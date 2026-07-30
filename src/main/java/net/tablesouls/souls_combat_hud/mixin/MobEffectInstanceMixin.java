package net.tablesouls.souls_combat_hud.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.tablesouls.souls_combat_hud.accessor.IEffectDurationAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements IEffectDurationAccessor {

    @Shadow
    private int duration; // duration

    @Unique
    private int souls_combat_hud$maxDuration;

    @Override
    public int souls_combat_hud$getMaxDuration() {
        return this.souls_combat_hud$maxDuration;
    }

    @Override
    public void souls_combat_hud$setMaxDuration(int maxDuration) {
        this.souls_combat_hud$maxDuration = maxDuration;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffect;IIZZZLnet/minecraft/world/effect/MobEffectInstance;Ljava/util/Optional;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.souls_combat_hud$maxDuration = this.duration;
    }

    @Inject(method = "setDetailsFrom(Lnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("RETURN"))
    private void onSetDetailsFrom(MobEffectInstance that, CallbackInfo ci) {
        this.souls_combat_hud$maxDuration = ((IEffectDurationAccessor) that).souls_combat_hud$getMaxDuration();
    }

    @Inject(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/world/effect/MobEffectInstance;duration:I", opcode = 181))
    private void onUpdate(MobEffectInstance that, CallbackInfoReturnable<Boolean> cir) {
        this.souls_combat_hud$maxDuration = ((IEffectDurationAccessor) that).souls_combat_hud$getMaxDuration();
    }
}