package net.tablesouls.souls_combat_hud.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class GuiOffhandMixin {
    @Redirect(
            method = "renderHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack souls_combat_hud$hideVanillaOffhandSlot(Player player) {
        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideOffhandSlot.get()) {
            return ItemStack.EMPTY;
        }
        return player.getOffhandItem();
    }
}
