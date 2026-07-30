package net.tablesouls.souls_combat_hud.party;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FTBTeamsPartyMembershipSource implements PartyMembershipSource {

    @Override
    public TeamSourceMode mode() {
        return TeamSourceMode.FTB_TEAMS;
    }

    @Override
    public boolean isAvailable(ServerPlayer player) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            return false;
        }

        TeamManager manager = FTBTeamsAPI.api().getManager();
        Optional<Team> team = manager.getTeamForPlayer(player);

        return team.isPresent() && team.get().isPartyTeam() && team.get().getMembers().size() > 1;
    }

    @Override
    public List<UUID> resolveTeammateIds(ServerPlayer player) {
        TeamManager manager = FTBTeamsAPI.api().getManager();
        Optional<Team> team = manager.getTeamForPlayer(player);

        if (team.isEmpty() || !team.get().isPartyTeam()) {
            return List.of();
        }

        return team.get().getMembers().stream()
                .filter(id -> !id.equals(player.getUUID()))
                .toList();
    }
}