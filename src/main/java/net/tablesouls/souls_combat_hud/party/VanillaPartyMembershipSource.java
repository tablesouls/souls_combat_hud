package net.tablesouls.souls_combat_hud.party;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VanillaPartyMembershipSource implements PartyMembershipSource {

    @Override
    public TeamSourceMode mode() {
        return TeamSourceMode.VANILLA;
    }

    @Override
    public boolean isAvailable(ServerPlayer player) {
        return player.getTeam() != null;
    }

    @Override
    public List<UUID> resolveTeammateIds(ServerPlayer player) {
        Team team = player.getTeam();
        if (team == null) return List.of();

        List<UUID> teammates = new ArrayList<>();
        for (String name : team.getPlayers()) {
            if (name.equals(player.getGameProfile().getName())) continue;

            ServerPlayer teammate = player.getServer().getPlayerList().getPlayerByName(name);
            if (teammate != null) {
                teammates.add(teammate.getUUID());
            }
        }
        return teammates;
    }
}