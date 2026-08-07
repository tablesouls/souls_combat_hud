package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

import java.util.function.Supplier;

public class ManaSourcePacket {
    private final ManaSourceMode mode;

    public ManaSourcePacket(ManaSourceMode mode) {
        this.mode = mode;
    }

    public static void encode(ManaSourcePacket packet, FriendlyByteBuf buf) {
        ResourceModeCodec.encode(packet.mode, buf);
    }

    public static ManaSourcePacket decode(FriendlyByteBuf buf) {
        return new ManaSourcePacket(ResourceModeCodec.decode(buf, ManaSourceMode.class));
    }

    public static void handle(ManaSourcePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> ManaSourceRegistry.INSTANCE.setActiveMode(packet.mode));
        ctx.setPacketHandled(true);
    }
}
