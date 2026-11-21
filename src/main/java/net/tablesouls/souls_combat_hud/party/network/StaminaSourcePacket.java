package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

public class StaminaSourcePacket implements CustomPacketPayload {
    public static final Type<StaminaSourcePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "stamina_source"));
    public static final StreamCodec<FriendlyByteBuf, StaminaSourcePacket> STREAM_CODEC =
            StreamCodec.ofMember(StaminaSourcePacket::encode, StaminaSourcePacket::decode);

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

    public static void handle(StaminaSourcePacket packet, IPayloadContext ctx) {
        // This packet is only ever sent server -> client.
        ctx.enqueueWork(() -> StaminaSourceRegistry.INSTANCE.setActiveMode(packet.mode));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
