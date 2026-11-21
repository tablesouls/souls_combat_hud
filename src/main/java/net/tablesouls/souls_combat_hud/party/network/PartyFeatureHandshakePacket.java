package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

public class PartyFeatureHandshakePacket implements CustomPacketPayload {
    public static final Type<PartyFeatureHandshakePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party_feature_handshake"));
    public static final StreamCodec<FriendlyByteBuf, PartyFeatureHandshakePacket> STREAM_CODEC =
            StreamCodec.ofMember(PartyFeatureHandshakePacket::encode, PartyFeatureHandshakePacket::decode);

    private final boolean enabled;

    public PartyFeatureHandshakePacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(PartyFeatureHandshakePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.enabled);
    }

    public static PartyFeatureHandshakePacket decode(FriendlyByteBuf buf) {
        return new PartyFeatureHandshakePacket(buf.readBoolean());
    }

    public static void handle(PartyFeatureHandshakePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (packet.enabled) {
                PartyNetwork.markServerSupportsParty();
                PartyNetwork.sendPrivacySetting(SoulsCombatHUDConfig.STATUS_GAUGE.hideStatusFromParty.get());
            } else {
                PartyNetwork.resetServerSupportsParty();
                PartyMemberClientCache.clearAll();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}