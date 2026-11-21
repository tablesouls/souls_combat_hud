package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.minecraft.server.level.ServerPlayer;
import net.tablesouls.souls_combat_hud.party.server.PartyPrivacyRegistry;
import net.tablesouls.souls_combat_hud.party.server.PartyTrackerRegistry;

import java.util.UUID;

public class PartyPrivacyPacket implements CustomPacketPayload {
    public static final Type<PartyPrivacyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party_privacy"));
    public static final StreamCodec<FriendlyByteBuf, PartyPrivacyPacket> STREAM_CODEC =
            StreamCodec.ofMember(PartyPrivacyPacket::encode, PartyPrivacyPacket::decode);

    public final boolean hidden;

    public PartyPrivacyPacket(boolean hidden) {
        this.hidden = hidden;
    }

    public static void encode(PartyPrivacyPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.hidden);
    }

    public static PartyPrivacyPacket decode (FriendlyByteBuf buf) {
        return new PartyPrivacyPacket(buf.readBoolean());
    }

    public static void handle(PartyPrivacyPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(()-> {
            ServerPlayer sender = (ServerPlayer) ctx.player();

            if (sender != null) {
                PartyPrivacyRegistry.setHidden(sender.getUUID(), packet.hidden);

                UUID subject = sender.getUUID();
                for (UUID trackerId : PartyTrackerRegistry.getTrackers(subject)) {
                    ServerPlayer tracker = sender.getServer().getPlayerList().getPlayer(trackerId);
                    if (tracker != null) {
                        PartyServerEvents.syncKnownStats(sender, tracker);
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}