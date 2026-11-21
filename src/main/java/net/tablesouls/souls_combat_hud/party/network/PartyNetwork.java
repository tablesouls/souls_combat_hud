package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.PartyStatType;

import java.util.UUID;

public final class PartyNetwork {
    private static volatile boolean serverSupportsParty = false;

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(PartyStatUpdatePacket.TYPE, PartyStatUpdatePacket.STREAM_CODEC, PartyStatUpdatePacket::handle);
        registrar.playToClient(PartyFeatureHandshakePacket.TYPE, PartyFeatureHandshakePacket.STREAM_CODEC, PartyFeatureHandshakePacket::handle);
        registrar.playToClient(PartyTeamSourcePacket.TYPE, PartyTeamSourcePacket.STREAM_CODEC, PartyTeamSourcePacket::handle);
        registrar.playToClient(ManaSourcePacket.TYPE, ManaSourcePacket.STREAM_CODEC, ManaSourcePacket::handle);
        registrar.playToClient(StaminaSourcePacket.TYPE, StaminaSourcePacket.STREAM_CODEC, StaminaSourcePacket::handle);
        registrar.playToServer(PartyPrivacyPacket.TYPE, PartyPrivacyPacket.STREAM_CODEC, PartyPrivacyPacket::handle);
        registrar.playToClient(PartyPrivacyStatePacket.TYPE, PartyPrivacyStatePacket.STREAM_CODEC, PartyPrivacyStatePacket::handle);
        registrar.playToClient(ConfigReloadPacket.TYPE, ConfigReloadPacket.STREAM_CODEC, ConfigReloadPacket::handle);
    }

    public static void sendStatUpdate(ServerPlayer to, UUID subject, PartyStatType type, Object value) {
        PacketDistributor.sendToPlayer(to, new PartyStatUpdatePacket(subject, type, value));
    }

    public static void sendFeatureHandshake(ServerPlayer to, boolean enabled) {
        PacketDistributor.sendToPlayer(to, new PartyFeatureHandshakePacket(enabled));
    }

    public static void sendTeamSource(ServerPlayer to, TeamSourceMode mode) {
        PacketDistributor.sendToPlayer(to, new PartyTeamSourcePacket(mode));
    }

    public static void sendManaSource(ServerPlayer to, ManaSourceMode mode) {
        PacketDistributor.sendToPlayer(to, new ManaSourcePacket(mode));
    }

    public static void sendStaminaSource(ServerPlayer to, StaminaSourceMode mode) {
        PacketDistributor.sendToPlayer(to, new StaminaSourcePacket(mode));
    }

    public static void sendPrivacySetting(boolean hidden) {
        PacketDistributor.sendToServer(new PartyPrivacyPacket(hidden));
    }

    public static void sendPrivacyState(ServerPlayer to, UUID subject, boolean hidden) {
        PacketDistributor.sendToPlayer(to, new PartyPrivacyStatePacket(subject, hidden));
    }

    public static void broadcastConfigReload() {
        PacketDistributor.sendToAllPlayers(ConfigReloadPacket.fromCurrentServerValues());
        boolean partyTrackingEnabled = !SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disablePartyTracking.get();
        PacketDistributor.sendToAllPlayers(new PartyFeatureHandshakePacket(partyTrackingEnabled));
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
