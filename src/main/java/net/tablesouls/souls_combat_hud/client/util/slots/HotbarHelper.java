package net.tablesouls.souls_combat_hud.client.util.slots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tablesouls.souls_combat_hud.compat.autohud.AutoHudCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

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

    public static void setSelectedSlot(Player player, int slot) {
        player.getInventory().selected = slot;

        if (!SoulsCombatHUDConfig.VISIBILITY.autoHud.hotbarRemainHidden.get()) return;
        AutoHudCompat.suppressHotbarReveal();

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }
}