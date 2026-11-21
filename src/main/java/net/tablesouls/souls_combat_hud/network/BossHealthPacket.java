package net.tablesouls.souls_combat_hud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.util.BossBarState;

import java.util.UUID;

public class BossHealthPacket implements CustomPacketPayload {
    public static final Type<BossHealthPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "boss_health"));
    public static final StreamCodec<FriendlyByteBuf, BossHealthPacket> STREAM_CODEC =
            StreamCodec.ofMember(BossHealthPacket::encode, BossHealthPacket::decode);

    private final UUID id;
    private final float current;
    private final float max;

    public BossHealthPacket(UUID id, float current, float max) {
        this.id = id; this.current = current; this.max = max;
    }

    public static void encode(BossHealthPacket p, FriendlyByteBuf buf) {
        buf.writeUUID(p.id);
        buf.writeFloat(p.current);
        buf.writeFloat(p.max);
    }

    public static BossHealthPacket decode(FriendlyByteBuf buf) {
        return new BossHealthPacket(buf.readUUID(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(BossHealthPacket p, IPayloadContext ctx) {
        ctx.enqueueWork(() ->BossBarState.updateHealth(p.id, p.current, p.max));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
