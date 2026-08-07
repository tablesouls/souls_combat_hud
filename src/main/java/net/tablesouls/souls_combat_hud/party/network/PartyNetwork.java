package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.PartyStatType;

import java.util.UUID;

public final class PartyNetwork {
    private static final String PROTOCOL_VERSION = "5";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party"),
            () -> PROTOCOL_VERSION,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
    );

    private static volatile boolean serverSupportsParty = false;

    private static int nextId = 0;

    public static void register() {
        CHANNEL.registerMessage(nextId++, PartyStatUpdatePacket.class,
                PartyStatUpdatePacket::encode,
                PartyStatUpdatePacket::decode,
                PartyStatUpdatePacket::handle);
        CHANNEL.registerMessage(nextId++, PartyFeatureHandshakePacket.class,
                PartyFeatureHandshakePacket::encode,
                PartyFeatureHandshakePacket::decode,
                PartyFeatureHandshakePacket::handle);
        CHANNEL.registerMessage(nextId++, PartyTeamSourcePacket.class,
                PartyTeamSourcePacket::encode,
                PartyTeamSourcePacket::decode,
                PartyTeamSourcePacket::handle);
        CHANNEL.registerMessage(nextId++, ManaSourcePacket.class,
                ManaSourcePacket::encode,
                ManaSourcePacket::decode,
                ManaSourcePacket::handle);
        CHANNEL.registerMessage(nextId++, StaminaSourcePacket.class,
                StaminaSourcePacket::encode,
                StaminaSourcePacket::decode,
                StaminaSourcePacket::handle);
        CHANNEL.registerMessage(nextId++, PartyPrivacyPacket.class,
                PartyPrivacyPacket::encode,
                PartyPrivacyPacket::decode,
                PartyPrivacyPacket::handle);
        CHANNEL.registerMessage(nextId++, PartyPrivacyStatePacket.class,
                PartyPrivacyStatePacket::encode,
                PartyPrivacyStatePacket::decode,
                PartyPrivacyStatePacket::handle);
        CHANNEL.registerMessage(nextId++, ConfigReloadPacket.class,
                ConfigReloadPacket::encode,
                ConfigReloadPacket::decode,
                ConfigReloadPacket::handle);
    }

    public static void sendStatUpdate(ServerPlayer to, UUID subject, PartyStatType type, Object value) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new PartyStatUpdatePacket(subject, type, value));
    }

    public static void sendFeatureHandshake(ServerPlayer to, boolean enabled) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new PartyFeatureHandshakePacket(enabled));
    }

    public static void sendTeamSource(ServerPlayer to, TeamSourceMode mode) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new PartyTeamSourcePacket(mode));
    }

    public static void sendManaSource(ServerPlayer to, ManaSourceMode mode) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new ManaSourcePacket(mode));
    }

    public static void sendStaminaSource(ServerPlayer to, StaminaSourceMode mode) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new StaminaSourcePacket(mode));
    }

    public static void sendPrivacySetting(boolean hidden) {
        CHANNEL.sendToServer(new PartyPrivacyPacket(hidden));
    }

    public static void sendPrivacyState(ServerPlayer to, UUID subject, boolean hidden) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new PartyPrivacyStatePacket(subject, hidden));
    }

    public static void broadcastConfigReload() {
        CHANNEL.send(PacketDistributor.ALL.noArg(), ConfigReloadPacket.fromCurrentServerValues());
        boolean partyTrackingEnabled = !SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disablePartyTracking.get();
        CHANNEL.send(PacketDistributor.ALL.noArg(), new PartyFeatureHandshakePacket(partyTrackingEnabled));
    }

    public static void markServerSupportsParty() {
        serverSupportsParty = true;
    }

    public static void resetServerSupportsParty() {
        serverSupportsParty = false;
    }

    public static boolean serverSupportsParty() {
        return serverSupportsParty;
    }
}