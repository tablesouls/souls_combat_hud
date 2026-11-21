package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

import java.util.UUID;

public class PartyPrivacyStatePacket implements CustomPacketPayload {
    public static final Type<PartyPrivacyStatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party_privacy_state"));
    public static final StreamCodec<FriendlyByteBuf, PartyPrivacyStatePacket> STREAM_CODEC =
            StreamCodec.ofMember(PartyPrivacyStatePacket::encode, PartyPrivacyStatePacket::decode);

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

    public static void handle(PartyPrivacyStatePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PartyMemberClientCache.setHidden(packet.subject, packet.hidden));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}