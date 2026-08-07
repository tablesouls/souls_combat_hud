package net.tablesouls.souls_combat_hud.party.server;

import net.minecraft.server.level.ServerPlayer;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PartyMembershipRegistry {
    private static final List<PartyMembershipSource> SOURCES = new ArrayList<>();

    static {
        register(new VanillaPartyMembershipSource());
        if (FTBTeamsCompat.LOADED) {
            register(new FTBTeamsPartyMembershipSource());
        }
    }

    public static void register(PartyMembershipSource source) {
        SOURCES.add(0, source);
    }

    public record Resolution(TeamSourceMode mode, List<UUID> teammateIds) {
        static final Resolution NONE = new Resolution(null, List.of());
    }

    public static Resolution resolve(ServerPlayer player) {
        TeamSourceMode forced = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.forceTeamSource.get();
        boolean pinned = !forced.isAuto();

        for (PartyMembershipSource source : SOURCES) {
            if (pinned && source.mode() != forced) {
                continue; // pinned to a single source -- skip anything else even if available
            }
            if (source.isAvailable(player)) {
                return new Resolution(source.mode(), source.resolveTeammateIds(player));
            }
        }
        return Resolution.NONE;
    }

    public static List<UUID> resolveTeammateIds(ServerPlayer player) {
        return resolve(player).teammateIds();
    }
}