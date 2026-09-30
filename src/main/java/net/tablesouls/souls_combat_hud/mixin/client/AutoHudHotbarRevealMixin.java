package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.compat.autohud.AutoHudCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mod.crend.autohud.component.Component", remap = false)
public abstract class AutoHudHotbarRevealMixin {
    @Shadow @Final public ResourceLocation identifier;

    @Inject(method = "revealCombined()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void souls_combat_hud$suppressOwnSlotSwitchReveal(CallbackInfo ci) {
        if (AutoHudCompat.isSuppressingHotbarReveal()
                && identifier != null
                && "autohud".equals(identifier.getNamespace())
                && "hotbar".equals(identifier.getPath())) {
            ci.cancel();
        }
    }
}