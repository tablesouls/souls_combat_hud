package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;

public class ManaSourcePacket implements CustomPacketPayload {
    public static final Type<ManaSourcePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "mana_source"));
    public static final StreamCodec<FriendlyByteBuf, ManaSourcePacket> STREAM_CODEC =
            StreamCodec.ofMember(ManaSourcePacket::encode, ManaSourcePacket::decode);

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

    public static void handle(ManaSourcePacket packet, IPayloadContext ctx) {
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> ManaSourceRegistry.INSTANCE.setActiveMode(packet.mode));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
