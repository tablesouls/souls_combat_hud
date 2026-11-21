package net.tablesouls.souls_combat_hud.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BossNetwork {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(BossHealthPacket.TYPE, BossHealthPacket.STREAM_CODEC, BossHealthPacket::handle);
    }

    public static void sendHealth(ServerPlayer to, java.util.UUID id, float current, float max) {
        PacketDistributor.sendToPlayer(to, new BossHealthPacket(id, current, max));
    }

    public static void sendHealthToTrackers(Entity entity, java.util.UUID id, float current, float max) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, new BossHealthPacket(id, current, max));
    }
}
