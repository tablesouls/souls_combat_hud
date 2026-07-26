package net.tablesouls.souls_combat_hud.util.slots;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksCompat;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.util.HotbarHelper;
import net.tablesouls.souls_combat_hud.util.SoundHelper;

import java.util.ArrayList;
import java.util.List;

public class ConsumableSlotManager {

    private static int selectedIndex = 0;
    private static Item lastSelectedItem = null;
    private static int lastSelectedSlot = -1;

    private ConsumableSlotManager() {
    }

    public static boolean isConsumable(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return false;
        }
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food != null) {
            return true;
        }
        if (IronsSpellbooksCompat.isScroll(stack)) {
            return true;
        }
        return stack.getItem() instanceof PotionItem;
    }

    private static List<Integer> getConsumableHotbarSlots(Player player) {
        return HotbarHelper.getMatchingHotbarSlots(player, stack -> isConsumable(stack, player));
    }

    public static List<ItemStack> getConsumables(Player player) {
        List<ItemStack> items = player.getInventory().items;
        return getConsumableHotbarSlots(player).stream().map(items::get).toList();
    }


    public static void cycle(Player player) {
        List<Integer> slots = getConsumableHotbarSlots(player);
        if (slots.size() <= 1) {
            if (slots.isEmpty()) {
                selectedIndex = 0;
                lastSelectedItem = null;
                lastSelectedSlot = -1;
            }
            return;
        }
        SoundHelper.playUiSound(ModSounds.CYCLE_CONSUMABLE.get());
        selectedIndex = (selectedIndex + 1) % slots.size();
        int slot = slots.get(selectedIndex);
        lastSelectedItem = player.getInventory().items.get(slot).getItem();
        lastSelectedSlot = slot;
    }

    private static List<Integer> resync(Player player) {
        List<Integer> slots = ConsumableSlotManager.getConsumableHotbarSlots(player);
        if (slots.isEmpty()) {
            selectedIndex = 0;
            lastSelectedSlot = -1;
            return slots;
        }
        if (selectedIndex >= slots.size()) {
            selectedIndex = 0;
        }
        int idx = slots.indexOf(lastSelectedSlot);
        if (idx >= 0) {
            selectedIndex = idx; // still on the exact slot we picked — keep it
        } else if (lastSelectedItem != null) {
            NonNullList items = player.getInventory().items;
            for (int i = 0; i < slots.size(); i++) {
                if (((ItemStack) items.get(slots.get(i))).getItem() == lastSelectedItem) {
                    selectedIndex = i;
                    break;
                }
            }
        }
        return slots;
    }

    public static ItemStack getSelected(Player player) {
        List<Integer> slots = ConsumableSlotManager.resync(player);
        if (slots.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int slot = slots.get(selectedIndex);
        ItemStack selected = player.getInventory().items.get(slot);
        lastSelectedItem = selected.getItem();
        lastSelectedSlot = slot;
        return selected;
    }

    public static int getSelectedHotbarSlot(Player player) {
        List<Integer> slots = resync(player);
        return slots.isEmpty() ? -1 : slots.get(selectedIndex);
    }

    public static int getSelectedIndexDisplay(Player player) {
        List<Integer> slots = resync(player);
        return slots.isEmpty() ? 0 : selectedIndex + 1;
    }

    public static int getConsumableCount(Player player) {
        return getConsumableHotbarSlots(player).size();
    }

    public static boolean hasMultipleConsumables(Player player) {
        return getConsumableCount(player) > 1;
    }

    public static ItemStack getPreview(Player player) {
        List<Integer> slots = resync(player);
        if (slots.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int nextIndex = (selectedIndex + 1) % slots.size();
        return player.getInventory().items.get(slots.get(nextIndex));
    }

    public static List<ItemStack> getPreviews(Player player, int count) {
        List<Integer> slots = resync(player);
        if (slots.isEmpty() || count <= 0) {
            return List.of();
        }
        int size = slots.size();
        int max = Math.min(count, size - 1); // exclude the currently selected slot
        List<ItemStack> result = new ArrayList<>(max);
        for (int offset = 1; offset <= max; offset++) {
            int idx = (selectedIndex + offset) % size;
            result.add(player.getInventory().items.get(slots.get(idx)));
        }
        return result;
    }
}