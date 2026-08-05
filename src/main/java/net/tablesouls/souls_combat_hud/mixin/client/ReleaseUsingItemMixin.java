package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.util.AutoConsumeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ReleaseUsingItemMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void soulsCombatHUD$preventEarlyRelease(Player player, CallbackInfo ci) {
        if (AutoConsumeHelper.isAutoConsuming(player.getUseItem())) {
            ci.cancel();
        }
    }
}