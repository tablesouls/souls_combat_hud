package net.tablesouls.souls_combat_hud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.client.util.BossBarState;

import java.util.UUID;
import java.util.function.Supplier;

public class BossHealthPacket {
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

    public static void handle(BossHealthPacket p, Supplier<NetworkEvent.Context> ctx)  {
        ctx.get().enqueueWork(() ->BossBarState.updateHealth(p.id, p.current, p.max));
        ctx.get().setPacketHandled(true);
    }
}
