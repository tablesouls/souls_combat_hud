package net.tablesouls.souls_combat_hud.compat.moreoffhandslots;

import net.akkynaa.moreoffhandslots.api.OffhandInventory;
import net.akkynaa.moreoffhandslots.client.config.ClientConfig;
import net.akkynaa.moreoffhandslots.network.payload.CycleOffhandPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.client.util.SoundHelper;

import java.util.ArrayList;
import java.util.List;

public class MoreOffhandSlotsCompat {
    public static final String MODID = "moreoffhandslots";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static void cycleOffhand(boolean next) {
        if (!LOADED) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (OffhandInventory.getOffhandItemsFromApi(player).isEmpty()) return;
        int emptySlotBehavior = ClientConfig.EMPTY_SLOT_BEHAVIOR.get().ordinal();
        SoundHelper.playUiSound(ModSounds.CYCLE_OFFHAND);
        PacketDistributor.sendToServer(new CycleOffhandPayload(next, emptySlotBehavior));
    }

    public static boolean hasMultipleOffhandItems(Player player) {
        if (!LOADED) return false;
        return nonEmptyItems(player).size() > 1;
    }

    public static List<ItemStack> getOffhandPreviews(Player player, int count) {
        if (!LOADED || count <= 0) return List.of();

        List<ItemStack> items = nonEmptyItems(player);
        int size = items.size();
        if (size <= 1) {
            return List.of();
        }

        int position = OffhandInventory.getRenderPosition(player);
        if (position < 0 || position >= size) {
            position = 0;
        }

        List<ItemStack> result = new ArrayList<>();
        for (int offset = 1; offset < size && result.size() < count; offset++) {
            result.add(items.get((position + offset) % size));
        }
        return result;
    }

    private static List<ItemStack> nonEmptyItems(Player player) {
        return OffhandInventory.getAllOffhandItemsInOrder(player).stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
    }
}