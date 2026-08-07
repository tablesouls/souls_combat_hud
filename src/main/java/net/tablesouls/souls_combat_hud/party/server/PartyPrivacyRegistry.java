package net.tablesouls.souls_combat_hud.party.server;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyPrivacyRegistry {
    private static final Set<UUID> HIDDEN = ConcurrentHashMap.newKeySet();

    public static void setHidden(UUID player, boolean hidden) {
        if (hidden) HIDDEN.add(player); else HIDDEN.remove(player);
    }

    public static boolean isHidden(UUID player) {
        return HIDDEN.contains(player);
    }

    public static void clear (UUID player) {HIDDEN.remove(player);}
}
