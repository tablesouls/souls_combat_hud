package net.tablesouls.souls_combat_hud.util.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.util.HotbarHelper;
import net.tablesouls.souls_combat_hud.util.RegexItemList;

import java.util.List;

public class WeaponSlotManager {
    private static final RegexItemList INCLUDE_WEAPONS =
            new RegexItemList(SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.includeWeaponsList);
    private static final RegexItemList EXCLUDE_WEAPONS =
            new RegexItemList(SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.excludeWeaponsList);

    private static int lastWeaponSlot = -1;

    public static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (EXCLUDE_WEAPONS.matches(stack)) return false;

        return INCLUDE_WEAPONS.matches(stack)
                || EpicFightCompat.isWeapon(stack)
                || stack.getItem() instanceof TieredItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem;
    }

    private static List<Integer> getWeaponHotbarSlots(Player player) {
        return HotbarHelper.getMatchingHotbarSlots(player, WeaponSlotManager::isWeapon);
    }

    public static void setLastWeaponSlot(int slot) {
        lastWeaponSlot = slot;
    }

    public static int getNextSlot(Player player) {
        List<Integer> slots = getWeaponHotbarSlots(player);
        if (slots.isEmpty()) return -1;

        int currentSlot = player.getInventory().selected;
        ItemStack currentStack = player.getInventory().items.get(currentSlot);

        if (!isWeapon(currentStack)) {
            if (slots.contains(lastWeaponSlot)) {
                return lastWeaponSlot;
            }
            return slots.get(0);
        }

        int currentIndex = slots.indexOf(currentSlot);
        int nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % slots.size();
        return slots.get(nextIndex);
    }

    public static int getJumpTargetSlot(Player player) {
        List<Integer> slots = getWeaponHotbarSlots(player);
        if (slots.isEmpty()) return -1;
        if (slots.contains(lastWeaponSlot)) {
            return lastWeaponSlot;
        }

        return slots.get(0);
    }
}