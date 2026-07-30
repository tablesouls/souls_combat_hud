package net.tablesouls.souls_combat_hud.compat.ftbteams;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.tablesouls.souls_combat_hud.party.PartyServerEvents;

import java.util.UUID;

public final class FTBTeamsPartyListener {
    public static void register() {
        TeamEvent.PLAYER_JOINED_PARTY.register(event ->
                recomputeForTeamMembers(event.getPlayer().getServer(), event.getTeam()));

        TeamEvent.PLAYER_LEFT_PARTY.register(event -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            if (!event.getTeamDeleted()) {
                recomputeForTeamMembers(server, event.getTeam());
            }

            ServerPlayer departing = server.getPlayerList().getPlayer(event.getPlayerId());
            if (departing != null) {
                PartyServerEvents.recomputeTrackersFor(departing);
            }
        });
    }

    private static void recomputeForTeamMembers(MinecraftServer server, Team team) {
        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                PartyServerEvents.recomputeTrackersFor(member);
            }
        }
    }
}