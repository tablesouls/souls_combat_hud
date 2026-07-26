package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class EpicFightCompat {
    public static final String MODID = "epicfight";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static boolean isWeapon(ItemStack stack) {
        if (!LOADED) return false;
        if (!stack.isEmpty()) {
            if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.includeCombatPreferred.get()
                && ClientConfig.combatPreferredItems.contains(stack.getItem())) {
                return true;
            }
        }
        CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
        return cap instanceof WeaponCapability || cap instanceof RangedWeaponCapability;
    }

    public static boolean canSwitchHoldingItem(Player player) {
        if (!LOADED) return true;
        LocalPlayerPatch playerpatch = (LocalPlayerPatch) ClientEngine.getInstance().getPlayerPatch();
        if (playerpatch == null) {
            return true;
        }
        return playerpatch.getEntityState().canSwitchHoldingItem();
    }
}
