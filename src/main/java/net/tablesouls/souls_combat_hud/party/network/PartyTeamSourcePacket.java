package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.function.Supplier;

public class PartyTeamSourcePacket {
    private final TeamSourceMode mode;

    public PartyTeamSourcePacket(TeamSourceMode mode) {
        this.mode = mode;
    }

    public static void encode(PartyTeamSourcePacket packet, FriendlyByteBuf buf) {
        ResourceModeCodec.encode(packet.mode, buf);
    }

    public static PartyTeamSourcePacket decode(FriendlyByteBuf buf) {
        return new PartyTeamSourcePacket(ResourceModeCodec.decode(buf, TeamSourceMode.class));
    }

    public static void handle(PartyTeamSourcePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> TeamProviderRegistry.setActiveMode(packet.mode));
        ctx.setPacketHandled(true);
    }
}