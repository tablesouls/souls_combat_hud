package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

import java.util.function.Supplier;

public class StaminaSourcePacket {
    private final StaminaSourceMode mode;

    public StaminaSourcePacket(StaminaSourceMode mode) {
        this.mode = mode;
    }

    public static void encode(StaminaSourcePacket packet, FriendlyByteBuf buf) {
        ResourceModeCodec.encode(packet.mode, buf);
    }

    public static StaminaSourcePacket decode(FriendlyByteBuf buf) {
        return new StaminaSourcePacket(ResourceModeCodec.decode(buf, StaminaSourceMode.class));
    }

    public static void handle(StaminaSourcePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> StaminaSourceRegistry.INSTANCE.setActiveMode(packet.mode));
        ctx.setPacketHandled(true);
    }
}
