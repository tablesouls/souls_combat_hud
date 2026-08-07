package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.party.server.PartyPrivacyRegistry;
import net.tablesouls.souls_combat_hud.party.server.PartyTrackerRegistry;

import java.util.UUID;
import java.util.function.Supplier;

public class PartyPrivacyPacket {
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

    public static void handle(PartyPrivacyPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(()-> {
            ServerPlayer sender = ctx.getSender();

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
        ctx.setPacketHandled(true);
    }
}