package net.tablesouls.souls_combat_hud.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AutoConsumeHelper {
    private static Item autoConsumeItem = null;

    public static void start(Item item) {
        autoConsumeItem = item;
    }

    public static void stop() {
        autoConsumeItem = null;
    }

    public static boolean isAutoConsuming(ItemStack stack) {
        return autoConsumeItem != null && !stack.isEmpty() && stack.getItem() == autoConsumeItem;
    }
}