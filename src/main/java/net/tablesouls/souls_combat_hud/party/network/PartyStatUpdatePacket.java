package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.party.PartyEffectSnapshot;
import net.tablesouls.souls_combat_hud.party.PartyStatType;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PartyStatUpdatePacket {
    private static final byte KIND_FLOAT = 0;
    private static final byte KIND_STRING = 1;
    private static final byte KIND_EFFECT_LIST = 2;
    private static final byte KIND_REMOVED = 3; // value == null: tells the client to forget this stat entirely

    private final UUID subject;
    private final PartyStatType type;
    private final Object value;

    public PartyStatUpdatePacket(UUID subject, PartyStatType type, Object value) {
        this.subject = subject;
        this.type = type;
        this.value = value;
    }

    public static void encode(PartyStatUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.subject);
        buf.writeEnum(packet.type);

        if (packet.value == null) {
            buf.writeByte(KIND_REMOVED);
        } else if (packet.value instanceof Float f) {
            buf.writeByte(KIND_FLOAT);
            buf.writeFloat(f);
        } else if (packet.value instanceof List<?> effects) {
            buf.writeByte(KIND_EFFECT_LIST);
            buf.writeVarInt(effects.size());
            for (Object entry : effects) {
                PartyEffectSnapshot snapshot = (PartyEffectSnapshot) entry;
                buf.writeResourceLocation(snapshot.effectId());
                buf.writeVarInt(snapshot.amplifier());
                buf.writeVarInt(snapshot.duration());
                buf.writeVarInt(snapshot.maxDuration());
            }
        } else {
            buf.writeByte(KIND_STRING);
            buf.writeUtf((String) packet.value);
        }
    }

    public static PartyStatUpdatePacket decode(FriendlyByteBuf buf) {
        UUID subject = buf.readUUID();
        PartyStatType type = buf.readEnum(PartyStatType.class);
        byte kind = buf.readByte();

        Object value = switch (kind) {
            case KIND_REMOVED -> null;
            case KIND_FLOAT -> buf.readFloat();
            case KIND_EFFECT_LIST -> {
                int size = buf.readVarInt();
                List<PartyEffectSnapshot> effects = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    ResourceLocation effectId = buf.readResourceLocation();
                    int amplifier = buf.readVarInt();
                    int duration = buf.readVarInt();
                    int maxDuration = buf.readVarInt();
                    effects.add(new PartyEffectSnapshot(effectId, amplifier, duration, maxDuration));
                }
                yield List.copyOf(effects);
            }
            default -> buf.readUtf();
        };

        return new PartyStatUpdatePacket(subject, type, value);
    }

    public static void handle(PartyStatUpdatePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (packet.value == null) {
                PartyMemberClientCache.remove(packet.subject, packet.type);
            } else {
                PartyMemberClientCache.update(packet.subject, packet.type, packet.value);
            }
        });
        ctx.setPacketHandled(true);
    }
}