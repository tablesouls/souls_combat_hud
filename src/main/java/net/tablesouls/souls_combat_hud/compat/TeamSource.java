package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

public interface TeamSource {
    TeamSourceMode mode();
    boolean isAvailable(AbstractClientPlayer localPlayer);
    List<UUID> resolveTeammateIds(AbstractClientPlayer localPlayer);
    OptionalInt resolveTeamColor(AbstractClientPlayer localPlayer);
}