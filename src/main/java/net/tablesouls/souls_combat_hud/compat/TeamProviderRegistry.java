package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsCompat;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsSource;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.client.PartyDisplayPreferences;
import net.tablesouls.souls_combat_hud.party.server.PartyJoinOrderTracker;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;

import java.util.*;

public final class TeamProviderRegistry {

    private static final Map<TeamSourceMode, TeamSource> BY_MODE = new EnumMap<>(TeamSourceMode.class);

    private static volatile TeamSourceMode activeMode = null;

    private static List<UUID> cachedBase = List.of();
    private static long cachedPrefsVersion = -1;
    private static List<UUID> cachedResult = List.of();

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
        invalidateDisplayedCache();
    }

    public static void reset() {
        activeMode = null;
        PartyMemberClientCache.clearAll();
        invalidateDisplayedCache();
    }

    private static void invalidateDisplayedCache() {
        cachedBase = List.of();
        cachedPrefsVersion = -1;
        cachedResult = List.of();
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
        return PartyJoinOrderTracker.sortNewestFirst(source.resolveTeammateIds(localPlayer));
    }

    public static List<UUID> resolveDisplayedTeammateIds(AbstractClientPlayer localPlayer) {
        List<UUID> base = resolveTeammateIds(localPlayer);
        long prefsVersion = PartyDisplayPreferences.version();

        if (prefsVersion == cachedPrefsVersion && base.equals(cachedBase)) {
            return cachedResult;
        }

        List<UUID> result = computeDisplayedTeammateIds(base);

        cachedBase = base;
        cachedPrefsVersion = prefsVersion;
        cachedResult = result;
        return result;
    }

    private static List<UUID> computeDisplayedTeammateIds(List<UUID> base) {
        if (base.isEmpty()) {
            return base;
        }

        List<UUID> visible = new ArrayList<>(base.size());
        for (UUID id : base) {
            if (!PartyDisplayPreferences.isHidden(id)) {
                visible.add(id);
            }
        }
        if (visible.isEmpty()) {
            return visible;
        }

        Map<UUID, Integer> fallbackIndex = new java.util.HashMap<>();
        for (int i = 0; i < visible.size(); i++) {
            fallbackIndex.put(visible.get(i), i);
        }

        Comparator<UUID> byPinnedFirst = Comparator.comparing(
                (UUID id) -> PartyDisplayPreferences.isPinned(id) ? 0 : 1
        );

        boolean sortOnlineFirst = SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.sortOnlineFirst.get();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        Comparator<UUID> byOnlineFirst = Comparator.comparing((UUID id) -> {
            if (!sortOnlineFirst) return 0;
            boolean online = connection != null && connection.getPlayerInfo(id) != null;
            return online ? 0 : 1;
        });

        Comparator<UUID> byCustomThenFallback = Comparator.comparing((UUID id) -> {
            Integer custom = PartyDisplayPreferences.getCustomOrder(id);
            return custom != null ? custom : fallbackIndex.get(id) + visible.size();
        });

        visible.sort(byPinnedFirst.thenComparing(byOnlineFirst).thenComparing(byCustomThenFallback));
        return visible;
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

    public static Optional<Component> resolveActiveTeamName(AbstractClientPlayer localPlayer) {
        TeamSourceMode mode = activeMode;
        if (mode == null) {
            return Optional.empty();
        }

        TeamSource source = BY_MODE.get(mode);
        if (source == null || !source.isAvailable(localPlayer)) {
            return Optional.empty();
        }
        return source.resolveTeamName(localPlayer);
    }
}