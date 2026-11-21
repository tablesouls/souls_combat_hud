package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.client.Minecraft;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "cn.mlus.thirst.foundation.gui.ThirstBarRenderer", remap = false)
public class ThirstBarRendererMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, remap = false)
    private static void souls_combat_hud$hideThirstBar(Minecraft mc, CallbackInfoReturnable<Boolean> cir) {
        if (SoulsCombatHUDConfig.VISIBILITY.thirstGui.hideThirst.get()) {
            cir.setReturnValue(false);
        }
    }
}