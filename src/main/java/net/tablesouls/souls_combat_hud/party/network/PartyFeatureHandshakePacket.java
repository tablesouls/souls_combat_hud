package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

import java.util.function.Supplier;

public class PartyFeatureHandshakePacket {
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

    public static void handle(PartyFeatureHandshakePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (packet.enabled) {
                PartyNetwork.markServerSupportsParty();
                PartyNetwork.sendPrivacySetting(SoulsCombatHUDConfig.STATUS_GAUGE.hideStatusFromParty.get());
            } else {
                PartyNetwork.resetServerSupportsParty();
                PartyMemberClientCache.clearAll();
            }
        });
        ctx.setPacketHandled(true);
    }
}