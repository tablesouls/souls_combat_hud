package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

import java.util.function.Supplier;

public class PartyFeatureHandshakePacket {

    public static void encode(PartyFeatureHandshakePacket packet, FriendlyByteBuf buf) {
        // no payload
    }

    public static PartyFeatureHandshakePacket decode(FriendlyByteBuf buf) {
        return new PartyFeatureHandshakePacket();
    }

    public static void handle(PartyFeatureHandshakePacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            PartyNetwork.markServerSupportsParty();
            PartyNetwork.sendPrivacySetting(SoulsCombatHUDConfig.STATUS_GAUGE.hideStatusFromParty.get());
        });
        ctx.setPacketHandled(true);
    }
}
