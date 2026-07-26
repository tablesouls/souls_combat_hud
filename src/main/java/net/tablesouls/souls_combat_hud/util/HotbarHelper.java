package net.tablesouls.souls_combat_hud.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HotbarHelper {
    public static final int HOTBAR_SIZE = 9;

    public static List<Integer> getMatchingHotbarSlots(Player player, Predicate<ItemStack> predicate) {
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            if (predicate.test(items.get(i))) {
                slots.add(i);
            }
        }
        return slots;
    }
}