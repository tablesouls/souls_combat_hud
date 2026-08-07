package net.tablesouls.souls_combat_hud.compat.moreoffhandslots;

import akkynaa.moreoffhandslots.api.OffhandInventory;
import akkynaa.moreoffhandslots.client.config.ClientConfig;
import akkynaa.moreoffhandslots.network.PacketHandler;
import akkynaa.moreoffhandslots.network.message.CycleOffhandMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.client.util.SoundHelper;

import java.util.ArrayList;
import java.util.List;

public class MoreOffhandSlotsCompat {
    public static final String MODID = "moreoffhandslots";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static void cycleOffhand(boolean next) {
        if(!LOADED) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (OffhandInventory.getOffhandItemsFromApi(player).isEmpty()) return;
        int emptySlotBehavior = ClientConfig.EMPTY_SLOT_BEHAVIOR.get().ordinal();
        SoundHelper.playUiSound(ModSounds.CYCLE_OFFHAND);
        PacketHandler.INSTANCE.sendToServer(new CycleOffhandMessage(next, emptySlotBehavior));
    }

    public static boolean hasMultipleOffhandItems(Player player) {
        if (!LOADED) return false;
        long nonEmpty = OffhandInventory.getAllOffhandItemsInOrder(player).stream()
                .filter(stack -> !stack.isEmpty())
                .count();
        return nonEmpty > 1;
    }

    public static List<ItemStack> getOffhandPreviews(Player player, int count) {
        if (!LOADED || count <= 0) return List.of();

        List<ItemStack> items = OffhandInventory.getAllOffhandItemsInOrder(player);
        int size = items.size();
        if (size <= 1) {
            return List.of();
        }

        int position = OffhandInventory.getPosition(player);
        if (position < 0 || position >= size) {
            position = 0;
        }

        List<ItemStack> result = new ArrayList<>();
        for (int offset = 1; offset < size && result.size() < count; offset++) {
            ItemStack stack = items.get((position + offset) % size);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }
}
