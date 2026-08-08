package net.tablesouls.souls_combat_hud.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;

public final class BossNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "boss"),
            () -> PROTOCOL_VERSION,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
    );

    public static void register() {
        CHANNEL.registerMessage(0, BossHealthPacket.class,
                BossHealthPacket::encode,
                BossHealthPacket::decode,
                BossHealthPacket::handle);
    }

    public static void sendHealth(ServerPlayer to, java.util.UUID id, float current, float max) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new BossHealthPacket(id, current, max));
    }

    public static void sendHealthToTrackers(Entity entity, java.util.UUID id, float current, float max) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new BossHealthPacket(id, current, max));
    }
}