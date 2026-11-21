package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

public class PartyTeamSourcePacket implements CustomPacketPayload {
    public static final Type<PartyTeamSourcePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party_team_source"));
    public static final StreamCodec<FriendlyByteBuf, PartyTeamSourcePacket> STREAM_CODEC =
            StreamCodec.ofMember(PartyTeamSourcePacket::encode, PartyTeamSourcePacket::decode);

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

    public static void handle(PartyTeamSourcePacket packet, IPayloadContext ctx) {
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> TeamProviderRegistry.setActiveMode(packet.mode));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}