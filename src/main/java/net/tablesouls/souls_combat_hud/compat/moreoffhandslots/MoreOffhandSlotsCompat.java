package net.tablesouls.souls_combat_hud.compat.moreoffhandslots;

import akkynaa.moreoffhandslots.api.OffhandInventory;
import akkynaa.moreoffhandslots.client.config.ClientConfig;
import akkynaa.moreoffhandslots.network.PacketHandler;
import akkynaa.moreoffhandslots.network.message.CycleOffhandMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.util.SoundHelper;

public class MoreOffhandSlotsCompat {
    public static final String MODID = "moreoffhandslots";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static void cycleOffhand(boolean next) {
        if(!LOADED) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (OffhandInventory.getOffhandItemsFromApi(player).isEmpty()) return;
        int emptySlotBehavior = ClientConfig.EMPTY_SLOT_BEHAVIOR.get().ordinal();
        SoundHelper.playUiSound(ModSounds.CYCLE_OFFHAND.get());
        PacketHandler.INSTANCE.sendToServer(new CycleOffhandMessage(next, emptySlotBehavior));
    }
}
