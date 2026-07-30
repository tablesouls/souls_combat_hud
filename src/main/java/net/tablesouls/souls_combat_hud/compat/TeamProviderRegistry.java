package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsCompat;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsSource;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.PartyMemberClientCache;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public final class TeamProviderRegistry {

    private static final Map<TeamSourceMode, TeamSource> BY_MODE = new EnumMap<>(TeamSourceMode.class);

    private static volatile TeamSourceMode activeMode = null;

    static {
        register(new VanillaTeamSource());
        if (FTBTeamsCompat.LOADED) {
            register(new FTBTeamsSource());
        }
    }

    private TeamProviderRegistry() {}

    public static void register(TeamSource source) {
        BY_MODE.put(source.mode(), source);
    }

    public static void setActiveMode(TeamSourceMode mode) {
        activeMode = mode;
    }

    public static void reset() {
        activeMode = null;
        PartyMemberClientCache.clearAll();
    }

    public static List<UUID> resolveTeammateIds(AbstractClientPlayer localPlayer) {
        TeamSourceMode mode = activeMode;
        if (mode == null) {
            return List.of();
        }

        TeamSource source = BY_MODE.get(mode);
        if (source == null || !source.isAvailable(localPlayer)) {
            return List.of();
        }
        return source.resolveTeammateIds(localPlayer);
    }

    public static OptionalInt resolveActiveTeamColor(AbstractClientPlayer localPlayer) {
        TeamSourceMode mode = activeMode;
        if (mode == null) {
            return OptionalInt.empty();
        }

        TeamSource source = BY_MODE.get(mode);
        if (source == null || !source.isAvailable(localPlayer)) {
            return OptionalInt.empty();
        }
        return source.resolveTeamColor(localPlayer);
    }
}