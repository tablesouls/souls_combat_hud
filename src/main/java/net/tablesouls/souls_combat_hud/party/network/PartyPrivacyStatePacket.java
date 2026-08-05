package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

import java.util.UUID;
import java.util.function.Supplier;

public class PartyPrivacyStatePacket {
    private final UUID subject;
    private final boolean hidden;

    public PartyPrivacyStatePacket(UUID subject, boolean hidden) {
        this.subject = subject;
        this.hidden = hidden;
    }

    public static void encode(PartyPrivacyStatePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.subject);
        buf.writeBoolean(packet.hidden);
    }

    public static PartyPrivacyStatePacket decode(FriendlyByteBuf buf) {
        return new PartyPrivacyStatePacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(PartyPrivacyStatePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PartyMemberClientCache.setHidden(packet.subject, packet.hidden));
        ctx.setPacketHandled(true);
    }
}