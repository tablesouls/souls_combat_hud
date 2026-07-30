package net.tablesouls.souls_combat_hud.party;

import net.minecraft.server.level.ServerPlayer;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.List;
import java.util.UUID;

public interface PartyMembershipSource {
    TeamSourceMode mode();
    boolean isAvailable(ServerPlayer player);
    List<UUID> resolveTeammateIds(ServerPlayer player);
}