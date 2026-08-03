package net.tablesouls.souls_combat_hud.compat.ftbteams;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tablesouls.souls_combat_hud.compat.TeamSource;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class FTBTeamsSource implements TeamSource {

    @Override
    public TeamSourceMode mode() {
        return TeamSourceMode.FTB_TEAMS;
    }

    @Override
    public boolean isAvailable(AbstractClientPlayer localPlayer) {
        if (!FTBTeamsCompat.LOADED || !FTBTeamsAPI.api().isClientManagerLoaded()) {
            return false;
        }
        Team team = FTBTeamsAPI.api().getClientManager().selfTeam();
        // Every player is always part of a team, so ensure there are actual teammates.
        return team != null && team.isPartyTeam() && team.getMembers().size() > 1;
    }

    @Override
    public List<UUID> resolveTeammateIds(AbstractClientPlayer localPlayer) {
        ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
        Team team = manager == null ? null : manager.selfTeam();
        if (team == null || !team.isPartyTeam()) {
            return List.of();
        }

        return team.getMembers().stream()
                .filter(id -> !id.equals(localPlayer.getUUID()))
                .toList();
    }

    @Override
    public OptionalInt resolveTeamColor(AbstractClientPlayer localPlayer) {
        if (!FTBTeamsCompat.LOADED || !FTBTeamsAPI.api().isClientManagerLoaded()) {
            return OptionalInt.empty();
        }

        ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
        Team team = manager == null ? null : manager.selfTeam();
        if (team == null) {
            return OptionalInt.empty();
        }

        Color4I color = team.getProperty(TeamProperties.COLOR);
        return (color == null || color.isEmpty()) ? OptionalInt.empty() : OptionalInt.of(color.rgb());
    }

    @Override
    public Optional<Component> resolveTeamName(AbstractClientPlayer localPlayer) {
        if (!FTBTeamsCompat.LOADED || !FTBTeamsAPI.api().isClientManagerLoaded()) {
            return Optional.empty();
        }

        ClientTeamManager manager = FTBTeamsAPI.api().getClientManager();
        Team team = manager == null ? null : manager.selfTeam();
        if (team == null) {
            return Optional.empty();
        }

        Color4I color = team.getProperty(TeamProperties.COLOR);
        Component name = team.getName();
        if (color != null && !color.isEmpty()) {
            name = name.copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.rgb())));
        }
        return Optional.of(name);
    }
}