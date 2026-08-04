package net.tablesouls.souls_combat_hud.party.client;

import net.tablesouls.souls_combat_hud.party.PartyStatType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PartyMemberClientCache {
    private static final Map<UUID, Map<PartyStatType, Object>> CACHE = new HashMap<>();

    public static void update(UUID player, PartyStatType type, Object value) {
        CACHE.computeIfAbsent(player, k -> new EnumMap<>(PartyStatType.class)).put(type, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(UUID player, PartyStatType type) {
        Map<PartyStatType, Object> stats = CACHE.get(player);
        return stats != null ? (T) stats.getOrDefault(type, type.empty()) : (T) type.empty();
    }

    public static boolean has(UUID player, PartyStatType type) {
        Map<PartyStatType, Object> stats = CACHE.get(player);
        return stats != null && stats.containsKey(type);
    }

    public static void clear(UUID player) {
        CACHE.remove(player);
    }

    public static void clearAll() {
        CACHE.clear();
    }
}