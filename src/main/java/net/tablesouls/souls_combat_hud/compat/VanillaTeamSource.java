package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Team;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberProfileCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class VanillaTeamSource implements TeamSource {

    @Override
    public TeamSourceMode mode() {
        return TeamSourceMode.VANILLA;
    }

    @Override
    public boolean isAvailable(AbstractClientPlayer localPlayer) {
        return localPlayer.getTeam() != null;
    }

    @Override
    public List<UUID> resolveTeammateIds(AbstractClientPlayer localPlayer) {
        Team team = localPlayer.getTeam();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (team == null || connection == null) {
            return List.of();
        }

        List<UUID> teammates = new ArrayList<>();
        for (String name : team.getPlayers()) {
            if (name.equals(localPlayer.getGameProfile().getName())) continue;

            PlayerInfo info = connection.getPlayerInfo(name);
            if (info != null) {
                teammates.add(info.getProfile().getId());
                continue;
            }

            UUID cachedId = PartyMemberProfileCache.getUuidForUsername(name);
            if (cachedId != null) {
                teammates.add(cachedId);
            }
        }
        return teammates;
    }

    @Override
    public OptionalInt resolveTeamColor(AbstractClientPlayer localPlayer) {
        Team team = localPlayer.getTeam();
        if (team == null) {
            return OptionalInt.empty();
        }

        Integer color = team.getColor().getColor();
        return color != null ? OptionalInt.of(color) : OptionalInt.empty();
    }

    @Override
    public Optional<Component> resolveTeamName(AbstractClientPlayer localPlayer) {
        Team team = localPlayer.getTeam();
        if (team == null) {
            return Optional.empty();
        }
        return Optional.of(Component.literal(team.getName()).withStyle(team.getColor()));
    }
}