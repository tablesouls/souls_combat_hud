package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PartyFeatureHandshakePacket {

    public static void encode(PartyFeatureHandshakePacket packet, FriendlyByteBuf buf) {
        // no payload
    }

    public static PartyFeatureHandshakePacket decode(FriendlyByteBuf buf) {
        return new PartyFeatureHandshakePacket();
    }

    public static void handle(PartyFeatureHandshakePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(PartyNetwork::markServerSupportsParty);
        ctx.setPacketHandled(true);
    }
}
